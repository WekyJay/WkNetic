package cn.wekyjay.wknetic.admin.game.service.impl;

import cn.wekyjay.wknetic.admin.game.service.GameChatControllerService;
import cn.wekyjay.wknetic.api.model.packet.ServerSessionPacket;
import cn.wekyjay.wknetic.common.model.vo.ServerStatusVO;
import cn.wekyjay.wknetic.common.utils.RedisUtils;
import cn.wekyjay.wknetic.socket.manager.ChannelManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * GameChatController服务实现类
 * 负责获取服务器状态、频道列表和世界列表等实时数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameChatControllerServiceImpl implements GameChatControllerService {
    
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ChannelManager channelManager;
    
    // Redis key patterns
    private static final String SERVER_STATUS_KEY_PREFIX = "wknetic:server:status:";
    private static final String SERVER_TOKENS_KEY = "wknetic:server:tokens";
    private static final String SERVER_ACTIVE_SESSION_KEY = "wknetic:server:active:sessions";
    
    @Override
    public List<ServerStatusVO> getServers() {
        try {
            // 优先从ChannelManager获取活跃服务器会话
            List<ServerSessionPacket> sessions = channelManager.getAllSessions();
            if (sessions.isEmpty()) {
                // 如果ChannelManager中没有数据，回退到Redis
                return getServersFromRedis();
            }
            
            List<ServerStatusVO> servers = new ArrayList<>();
            for (ServerSessionPacket session : sessions) {
                try {
                    ServerStatusVO status = convertToServerStatusVO(session);
                    servers.add(status);
                } catch (Exception e) {
                    log.warn("转换服务器会话数据失败 [sessionId: {}]: {}", session.getSessionId(), e.getMessage());
                }
            }
            
            return servers;
        } catch (Exception e) {
            log.error("获取服务器列表失败", e);
            return getDefaultServers();
        }
    }
    
    /**
     * 从Redis获取服务器数据（备用方案）
     */
    private List<ServerStatusVO> getServersFromRedis() {
        try {
            Set<String> activeSessions = stringRedisTemplate.opsForSet().members(SERVER_ACTIVE_SESSION_KEY);
            if (activeSessions == null || activeSessions.isEmpty()) {
                return Collections.emptyList();
            }
            
            List<ServerStatusVO> servers = new ArrayList<>();
            for (String sessionId : activeSessions) {
                String serverKey = SERVER_STATUS_KEY_PREFIX + sessionId;
                String serverData = stringRedisTemplate.opsForValue().get(serverKey);
                
                if (serverData != null) {
                    try {
                        ServerStatusVO status = objectMapper.readValue(serverData, ServerStatusVO.class);
                        status.setSessionId(sessionId);
                        servers.add(status);
                    } catch (Exception e) {
                        log.warn("解析服务器状态数据失败 [sessionId: {}]: {}", sessionId, e.getMessage());
                    }
                }
            }
            
            if (servers.isEmpty()) {
                return getDefaultServers();
            }
            
            return servers;
        } catch (Exception e) {
            log.error("从Redis获取服务器列表失败", e);
            return getDefaultServers();
        }
    }
    
    /**
     * 将ServerSessionPacket转换为ServerStatusVO
     */
    private ServerStatusVO convertToServerStatusVO(ServerSessionPacket session) {
        ServerStatusVO status = new ServerStatusVO();
        status.setSessionId(session.getSessionId());
        status.setServerName(session.getServerName());
        status.setIp(session.getLoginIp());
        status.setPort(session.getPort());
        status.setOnlinePlayers(session.getOnlinePlayers());
        status.setMaxPlayers(session.getMaxPlayers());
        status.setVersion(session.getVersion());
        status.setStatus("在线"); // ChannelManager中的服务器都在线
        status.setTps(session.getTps());
        status.setLastHeartbeat(session.getLastActiveTime() != null ? session.getLastActiveTime() : new Date());
        
        // 设置内存使用情况
        if (session.getRamUsage() != null && session.getMaxRam() != null) {
            status.setMemoryUsage(session.getRamUsage());
            status.setTotalMemory(session.getMaxRam());
        }
        
        // 设置插件信息
        if (session.getPluginList() != null) {
            status.setPluginCount(session.getPluginList().size());
            status.setPluginsEnabled(session.getPluginList().size() > 0);
        }
        
        return status;
    }
    
    @Override
    public List<Map<String, String>> getChannels() {
        // 这里可以从配置文件中读取频道列表
        // 暂时返回默认的频道列表
        return Arrays.asList(
            createChannel("global", "全局聊天"),
            createChannel("world", "世界聊天"),
            createChannel("party", "队伍聊天"),
            createChannel("guild", "公会聊天"),
            createChannel("whisper", "私聊"),
            createChannel("staff", "管理员频道"),
            createChannel("announcement", "公告频道")
        );
    }
    
    @Override
    public List<Map<String, String>> getWorlds() {
        // 这里可以从数据库或配置文件中读取世界列表
        // 暂时返回默认的世界列表
        return Arrays.asList(
            createWorld("all", "所有世界"),
            createWorld("world", "主世界"),
            createWorld("world_nether", "下界"),
            createWorld("world_the_end", "末地"),
            createWorld("resource", "资源世界"),
            createWorld("minigames", "小游戏世界")
        );
    }
    
    private List<ServerStatusVO> getDefaultServers() {
        List<ServerStatusVO> defaultServers = new ArrayList<>();
        defaultServers.add(createDefaultServer("main-server", "主服务器", 0, "未知"));
        defaultServers.add(createDefaultServer("test-server", "测试服务器", 0, "未知"));
        defaultServers.add(createDefaultServer("creative-server", "创造服务器", 0, "未知"));
        return defaultServers;
    }
    
    private ServerStatusVO createDefaultServer(String id, String name, int players, String version) {
        ServerStatusVO server = new ServerStatusVO();
        server.setSessionId(id);
        server.setServerName(name);
        server.setOnlinePlayers(players);
        server.setMaxPlayers(50);
        server.setVersion(version);
        server.setStatus("离线");
        server.setLastHeartbeat(new Date());
        server.setTps(20.0);
        return server;
    }
    
    private Map<String, String> createChannel(String id, String name) {
        Map<String, String> channel = new HashMap<>();
        channel.put("id", id);
        channel.put("name", name);
        return channel;
    }
    
    private Map<String, String> createWorld(String id, String name) {
        Map<String, String> world = new HashMap<>();
        world.put("id", id);
        world.put("name", name);
        return world;
    }
}