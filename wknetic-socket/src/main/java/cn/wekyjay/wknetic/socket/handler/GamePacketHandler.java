package cn.wekyjay.wknetic.socket.handler;

import cn.wekyjay.wknetic.api.enums.PacketType;
import cn.wekyjay.wknetic.api.model.packet.ServerLoginPacket;
import cn.wekyjay.wknetic.api.model.packet.ServerSessionPacket;
import cn.wekyjay.wknetic.common.domain.SysServerToken;

import cn.wekyjay.wknetic.common.mapper.SysServerTokenMapper;
import cn.wekyjay.wknetic.socket.manager.ChannelManager;



// 引入 Jackson
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.data.redis.core.StringRedisTemplate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;

import java.net.InetSocketAddress;
import java.util.Date;


@Slf4j
@Component
@ChannelHandler.Sharable
public class GamePacketHandler extends SimpleChannelInboundHandler<String> {

    @Resource
    private ChannelManager channelManager;
    
    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysServerTokenMapper serverTokenMapper;

    public static final String CHAT_TOPIC = "wknetic-global-chat";
    public static final String SERVER_STATUS_TOPIC = "wknetic:server:status";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.debug("RECV: {}", msg);

        try {
            JsonNode json = objectMapper.readTree(msg);
            
            if (!json.has("type")) return;
            int type = json.get("type").asInt();

            PacketType packetType = PacketType.getById(type);
            if (packetType == null) {
                // 兼容旧编号
                if (type == 3) {
                    handleGameChat(json);
                    return;
                }
                log.warn("Unknown packet type: {}", type);
                return;
            }

            switch (packetType) {
                case AUTH_REQUEST:
                case HANDSHAKE:
                case RECONNECT_REQUEST:
                case SERVER_LOGIN:
                    handleServerLogin(ctx, json);
                    break;

                case SERVER_HEARTBEAT:
                    handleServerHeartbeat(ctx, json);
                    break;

                case SERVER_INFO:
                    handleServerInfo(ctx, json);
                    break;

                case CHAT_MSG:
                case PRIVATE_MSG:
                case GROUP_CHAT:
                case SYSTEM_BROADCAST:
                    handleGameChat(json);
                    break;

                default:
                    log.warn("Unhandled packet type: {}", packetType);
                    break;
            }
        } catch (Exception e) {
            log.error("Packet parse error", e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 连接断开时清理
        channelManager.removeChannel(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Channel exception", cause);
        ctx.close();
    }

    /**
     * 处理游戏服务器登录
     */
    private void handleServerLogin(ChannelHandlerContext ctx, JsonNode json) {
        try {
            ServerLoginPacket loginPacket = objectMapper.treeToValue(json, ServerLoginPacket.class);
            String token = loginPacket.getToken();
            
            if (!StringUtils.hasText(token)) {
                sendServerResponse(ctx, PacketType.SERVER_LOGIN_RESP, false, "Token不能为空");
                ctx.close();
                return;
            }

            // 验证Token
            LambdaQueryWrapper<SysServerToken> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysServerToken::getTokenValue, token)
                   .eq(SysServerToken::getStatus, 1);
            SysServerToken serverToken = serverTokenMapper.selectOne(wrapper);

            if (serverToken == null) {
                sendServerResponse(ctx, PacketType.SERVER_LOGIN_RESP, false, "Token无效或已禁用");
                ctx.close();
                return;
            }

            // 获取登录IP
            String loginIp = getClientIp(ctx.channel());

            // 创建服务器会话
            ServerSessionPacket session = ServerSessionPacket.builder()
                    .token(token)
                    .serverName(loginPacket.getServerName())
                    .serverVersion(loginPacket.getServerVersion())
                    .loginIp(loginIp)
                    .loginTime(new Date())
                    .lastActiveTime(new Date())
                    .build();

            // 注册连接（单点登录）
            channelManager.registerChannel(token, ctx.channel(), session);

            // 更新数据库中的最后登录信息
            serverTokenMapper.updateLastLogin(token, loginIp);

            // 发送成功响应（包含sessionId）
            sendServerLoginResponse(ctx, session.getSessionId(), true, "登录成功");
            
            log.info("游戏服务器登录成功: {} [sessionId: {}]", loginPacket.getServerName(), session.getSessionId());
        } catch (Exception e) {
            log.error("处理服务器登录失败", e);
            sendServerResponse(ctx, PacketType.SERVER_LOGIN_RESP, false, "登录处理异常");
            ctx.close();
        }
    }

    /**
     * 处理服务器心跳
     */
    private void handleServerHeartbeat(ChannelHandlerContext ctx, JsonNode json) {
        ServerSessionPacket session = channelManager.getSession(ctx.channel());
        if (session != null) {
            session.setLastActiveTime(new Date());
            channelManager.updateSession(ctx.channel(), session);
        }
    }

    /**
     * 处理服务器信息更新
     */
    private void handleServerInfo(ChannelHandlerContext ctx, JsonNode json) {
        try {
            ServerSessionPacket infoPacket = objectMapper.treeToValue(json, ServerSessionPacket.class);
            
            ServerSessionPacket session = channelManager.getSession(ctx.channel());
            if (session == null) {
                log.warn("收到未认证连接的服务器信息");
                return;
            }

            // 更新会话信息
            session.setMotd(infoPacket.getMotd());
            session.setServerName(infoPacket.getServerName());
            session.setOnlinePlayers(infoPacket.getOnlinePlayers());
            session.setMaxPlayers(infoPacket.getMaxPlayers());
            session.setTps(infoPacket.getTps());
            session.setRamUsage(infoPacket.getRamUsage());
            session.setMaxRam(infoPacket.getMaxRam());
            session.setPlayerList(infoPacket.getPlayerList());
            session.setPluginList(infoPacket.getPluginList());
            session.setLastActiveTime(new Date());
            
            channelManager.updateSession(ctx.channel(), session);

            // 发布到Redis，供管理后台推送到前端
            // 设置sessionId字段（确保前端能正确识别服务器）
            infoPacket.setSessionId(session.getSessionId());
            String redisKey = SERVER_STATUS_TOPIC + ":" + session.getSessionId();
            stringRedisTemplate.convertAndSend(redisKey, objectMapper.writeValueAsString(infoPacket));
            
            log.debug("服务器状态更新: {} [sessionId: {}] - 在线玩家: {}/{}", session.getServerName(), 
                      session.getSessionId(), infoPacket.getOnlinePlayers(), infoPacket.getMaxPlayers());
        } catch (Exception e) {
            log.error("处理服务器信息失败", e);
        }
    }


    /**
     * 处理游戏聊天
     */
    private void handleGameChat(JsonNode json) {
        String playerName = json.has("player") ? json.get("player").asText() : "";
        String content = json.has("msg") ? json.get("msg").asText() : "";
        String server = json.has("server") ? json.get("server").asText() : "Unknown";
        String time = json.has("time") ? json.get("time").asText() : "";
        String world = json.has("world") ? json.get("world").asText() : "Global";
        String uuid = json.has("uuid") ? json.get("uuid").asText() : "";
        
        ObjectNode broadcastMsg = objectMapper.createObjectNode();
        broadcastMsg.put("player", playerName);
        broadcastMsg.put("content", content);
        broadcastMsg.put("server", server);
        broadcastMsg.put("uuid", uuid);
        broadcastMsg.put("time", time);
        broadcastMsg.put("world", world);

        stringRedisTemplate.convertAndSend(CHAT_TOPIC, broadcastMsg.toString());

        String historyKey = "wknetic:chat:history";
        stringRedisTemplate.opsForList().leftPush(historyKey, broadcastMsg.toString());
        stringRedisTemplate.opsForList().trim(historyKey, 0, 49);
        
        log.info("转发并保存聊天: [{}] {}", playerName, content);
    }

    /**
     * 发送服务器登录响应（包含sessionId）
     */
    private void sendServerLoginResponse(ChannelHandlerContext ctx, String sessionId, boolean success, String message) {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("type", PacketType.SERVER_LOGIN_RESP.getId());
        resp.put("success", success);
        resp.put("message", message);
        resp.put("sessionId", sessionId);
        resp.put("timestamp", System.currentTimeMillis());
        ctx.writeAndFlush(resp.toString());
    }

    /**
     * 发送服务器响应
     */
    private void sendServerResponse(ChannelHandlerContext ctx, PacketType type, boolean success, String message) {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("type", type.getId());
        resp.put("success", success);
        resp.put("message", message);
        resp.put("timestamp", System.currentTimeMillis());
        ctx.writeAndFlush(resp.toString());
    }


    /**
     * 获取客户端IP
     */
    private String getClientIp(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        return address.getAddress().getHostAddress();
    }
}