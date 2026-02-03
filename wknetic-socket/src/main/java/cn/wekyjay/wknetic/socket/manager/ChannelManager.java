package cn.wekyjay.wknetic.socket.manager;

import cn.wekyjay.wknetic.socket.model.ServerSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器连接管理器
 * 管理SessionId到Channel的映射和服务器会话信息
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Slf4j
@Component
public class ChannelManager {

    // SessionId -> Channel 映射（公开查询用）
    private static final ConcurrentHashMap<String, Channel> sessionChannelMap = new ConcurrentHashMap<>();
    
    // Channel -> ServerSession 映射
    private static final ConcurrentHashMap<ChannelId, ServerSession> sessionMap = new ConcurrentHashMap<>();
    
    // ChannelId -> SessionId 反向映射（用于快速移除）
    private static final ConcurrentHashMap<ChannelId, String> idToSessionIdMap = new ConcurrentHashMap<>();
    
    // Token -> SessionId 映射（仅用于认证和内部查询）
    private static final ConcurrentHashMap<String, String> tokenToSessionIdMap = new ConcurrentHashMap<>();

    /**
     * 注册服务器连接（单点登录：踢掉旧连接）
     * 
     * @param token Token值（用于认证）
     * @param channel Netty Channel
     * @param session 服务器会话信息
     * @return 是否成功注册
     */
    public boolean registerChannel(String token, Channel channel, ServerSession session) {
        String sessionId = session.getSessionId();
        
        // 检查是否有旧连接（通过token查询）
        String oldSessionId = tokenToSessionIdMap.get(token);
        if (oldSessionId != null) {
            Channel oldChannel = sessionChannelMap.get(oldSessionId);
            if (oldChannel != null && oldChannel.isActive()) {
                log.warn("检测到同一token的旧连接，踢出旧连接 [sessionId: {}]", oldSessionId);
                // 关闭旧连接（单点登录）
                oldChannel.close();
                // 清理旧连接映射
                sessionChannelMap.remove(oldSessionId);
                sessionMap.remove(oldChannel.id());
                idToSessionIdMap.remove(oldChannel.id());
            }
        }
        
        // 注册新连接
        sessionChannelMap.put(sessionId, channel);
        sessionMap.put(channel.id(), session);
        idToSessionIdMap.put(channel.id(), sessionId);
        tokenToSessionIdMap.put(token, sessionId);
        
        log.info("服务器 {} 注册成功 [sessionId: {}]", session.getServerName(), sessionId);
        return true;
    }

    /**
     * 移除连接
     * 
     * @param channel Netty Channel
     */
    public void removeChannel(Channel channel) {
        String sessionId = idToSessionIdMap.remove(channel.id());
        if (sessionId != null) {
            sessionChannelMap.remove(sessionId);
            ServerSession session = sessionMap.remove(channel.id());
            if (session != null) {
                // 移除token映射
                tokenToSessionIdMap.remove(session.getToken());
                log.info("服务器 {} 断开连接 [sessionId: {}]", session.getServerName(), sessionId);
            }
        }
    }

    /**
     * 根据SessionId获取Channel
     * 
     * @param sessionId 会话ID
     * @return Netty Channel
     */
    public Channel getChannelBySessionId(String sessionId) {
        return sessionChannelMap.get(sessionId);
    }
    
    /**
     * 根据Token获取Channel（内部使用，仅用于认证和兼容）
     * 
     * @param token Token值
     * @return Netty Channel
     * @deprecated 应使用 getChannelBySessionId 替代
     */
    @Deprecated
    public Channel getChannelByToken(String token) {
        String sessionId = tokenToSessionIdMap.get(token);
        if (sessionId != null) {
            return sessionChannelMap.get(sessionId);
        }
        return null;
    }
    
    /**
     * 根据Channel获取ServerSession
     * 
     * @param channel Netty Channel
     * @return ServerSession
     */
    public ServerSession getSession(Channel channel) {
        return sessionMap.get(channel.id());
    }
    
    /**
     * 根据SessionId获取ServerSession
     * 
     * @param sessionId 会话ID
     * @return ServerSession
     */
    public ServerSession getSessionBySessionId(String sessionId) {
        Channel channel = sessionChannelMap.get(sessionId);
        if (channel != null) {
            return sessionMap.get(channel.id());
        }
        return null;
    }
    
    /**
     * 根据Token获取ServerSession（内部使用，仅用于认证和兼容）
     * 
     * @param token Token值
     * @return ServerSession
     * @deprecated 应使用 getSessionBySessionId 替代
     */
    @Deprecated
    public ServerSession getSessionByToken(String token) {
        String sessionId = tokenToSessionIdMap.get(token);
        if (sessionId != null) {
            Channel channel = sessionChannelMap.get(sessionId);
            if (channel != null) {
                return sessionMap.get(channel.id());
            }
        }
        return null;
    }
    
    /**
     * 更新服务器会话信息
     * 
     * @param channel Netty Channel
     * @param session ServerSession
     */
    public void updateSession(Channel channel, ServerSession session) {
        sessionMap.put(channel.id(), session);
    }
    
    /**
     * 获取所有在线服务器会话
     * 
     * @return 服务器会话列表
     */
    public List<ServerSession> getAllSessions() {
        return new ArrayList<>(sessionMap.values());
    }
    
    /**
     * 获取在线服务器数量
     * 
     * @return 在线数量
     */
    public int size() {
        return sessionChannelMap.size();
    }
    
    /**
     * 根据Token获取SessionId（内部查询用）
     * 
     * @param token Token值
     * @return SessionId
     */
    public String getSessionIdByToken(String token) {
        return tokenToSessionIdMap.get(token);
    }
}