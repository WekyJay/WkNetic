package cn.wekyjay.wknetic.socket.handler;

import cn.wekyjay.wknetic.socket.manager.ChannelManager;

// 引入 Jackson
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.data.redis.core.StringRedisTemplate;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
@ChannelHandler.Sharable
public class GamePacketHandler extends SimpleChannelInboundHandler<String> {

    @Resource
    private ChannelManager channelManager;
    
    // ✅ 直接注入 Spring Boot 自带的 ObjectMapper
    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static final String CHAT_TOPIC = "wknetic-global-chat";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("RECV: {}", msg);

        try {
            // ✅ Jackson 解析方式：readTree
            // 类似于 Fastjson 的 JSONObject，Jackson 用 JsonNode
            JsonNode json = objectMapper.readTree(msg);
            
            // 安全取值
            if (!json.has("type")) return;
            int type = json.get("type").asInt();

            switch (type) {
                case 1: 
                    handleLogin(ctx, json);
                case 3:
                    handleGameChat(json);
                    break;
                // ... 其他 case
            }
        } catch (Exception e) {
            log.error("Packet parse error", e);
        }
    }

    // 处理器 - 游戏登录
    private void handleLogin(ChannelHandlerContext ctx, JsonNode json) {
        // Jackson 取字符串: node.get("key").asText()
        String token = json.has("token") ? json.get("token").asText() : "";

        // ... 验证逻辑不变 ...
        boolean isValid = true; 

        if (isValid) {
            channelManager.addChannel(token, ctx.channel());
            // 发送响应
            sendJson(ctx, 100, "Login Success");
        }
    }

    // 处理器 - 游戏聊天
    private void handleGameChat(JsonNode json) {
        // 1. 提取数据
        String playerName = json.has("player") ? json.get("player").asText() : "";
        String content = json.has("msg") ? json.get("msg").asText() : "";
        String server = json.has("server") ? json.get("server").asText() : "Unknown";
        String time = json.has("time") ? json.get("time").asText() : "";
        String world = json.has("world") ? json.get("world").asText() : "Global";
        String uuid = json.has("uuid") ? json.get("uuid").asText() : "";
        
        // 2. 构造要广播的数据 (可以加时间戳、服务器名等)
        ObjectNode broadcastMsg = objectMapper.createObjectNode();
        broadcastMsg.put("player", playerName);
        broadcastMsg.put("content", content);
        broadcastMsg.put("server", server);
        broadcastMsg.put("uuid", uuid);
        broadcastMsg.put("time", time);
        broadcastMsg.put("world", world);

        // 3. 【极速上报】推送到 Redis Channel
        stringRedisTemplate.convertAndSend(CHAT_TOPIC, broadcastMsg.toString());

        // ==========================================
        // 4. 【动作 B：存储】(给后来的人看)
        // 使用 Redis List 结构，存最近 50 条
        // ==========================================
        String historyKey = "wknetic:chat:history";
        
        // LPUSH: 从左边塞进去 (最新的在最上面)
        stringRedisTemplate.opsForList().leftPush(historyKey, broadcastMsg.toString());
        
        // LTRIM: 只保留前 50 条 (修剪列表)，防止 Redis 爆满
        stringRedisTemplate.opsForList().trim(historyKey, 0, 49);
        
        log.info("转发并保存聊天: [{}] {}", playerName, content);

    }


    /**
     * 发送响应消息
     * @param ctx
     * @param code
     * @param msg
     */
    private void sendJson(ChannelHandlerContext ctx, int code, String msg) {
        // ✅ Jackson 构造 JSON：createObjectNode
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("code", code);
        resp.put("msg", msg);
        
        // 转为字符串发送
        ctx.writeAndFlush(resp.toString());
    }
}