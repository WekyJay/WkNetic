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
 * 管理Token到Channel的映射和服务器会话信息
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Slf4j
@Component
public class ChannelManager {

    // Token -> Channel 映射
    private static final ConcurrentHashMap<String, Channel> tokenChannelMap = new ConcurrentHashMap<>();
    
    // Channel -> ServerSession 映射
    private static final ConcurrentHashMap<ChannelId, ServerSession> sessionMap = new ConcurrentHashMap<>();
    
    // ChannelId -> Token 反向映射（用于快速移除）
    private static final ConcurrentHashMap<ChannelId, String> idToTokenMap = new ConcurrentHashMap<>();

    /**
     * 注册服务器连接（单点登录：踢掉旧连接）
     * 
     * @param token Token值
     * @param channel Netty Channel
     * @param session 服务器会话信息
     * @return 是否成功注册
     */
    public boolean registerChannel(String token, Channel channel, ServerSession session) {
        // 检查是否有旧连接
        Channel oldChannel = tokenChannelMap.get(token);
        if (oldChannel != null && oldChannel.isActive()) {
            log.warn("Token {} 已存在连接，踢出旧连接", token);
            // 关闭旧连接（单点登录）
            oldChannel.close();
            // 清理旧连接映射
            sessionMap.remove(oldChannel.id());
            idToTokenMap.remove(oldChannel.id());
        }
        
        // 注册新连接
        tokenChannelMap.put(token, channel);
        sessionMap.put(channel.id(), session);
        idToTokenMap.put(channel.id(), token);
        
        log.info("服务器 {} 注册成功，Token: {}", session.getServerName(), token);
        return true;
    }

    /**
     * 移除连接
     * 
     * @param channel Netty Channel
     */
    public void removeChannel(Channel channel) {
        String token = idToTokenMap.remove(channel.id());
        if (token != null) {
            tokenChannelMap.remove(token);
            ServerSession session = sessionMap.remove(channel.id());
            if (session != null) {
                log.info("服务器 {} 断开连接，Token: {}", session.getServerName(), token);
            }
        }
    }

    /**
     * 根据Token获取Channel
     * 
     * @param token Token值
     * @return Netty Channel
     */
    public Channel getChannelByToken(String token) {
        return tokenChannelMap.get(token);
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
     * 根据Token获取ServerSession
     * 
     * @param token Token值
     * @return ServerSession
     */
    public ServerSession getSessionByToken(String token) {
        Channel channel = tokenChannelMap.get(token);
        if (channel != null) {
            return sessionMap.get(channel.id());
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
        return tokenChannelMap.size();
    }
    
    /**
     * 添加Channel（兼容旧方法）
     * 
     * @deprecated 使用 registerChannel 代替
     */
    @Deprecated
    public void addChannel(String token, Channel channel) {
        tokenChannelMap.put(token, channel);
        idToTokenMap.put(channel.id(), token);
    }
    
    /**
     * 根据Token获取Channel（兼容旧方法）
     * 
     * @deprecated 使用 getChannelByToken 代替
     */
    @Deprecated
    public Channel getChannel(String token) {
        return getChannelByToken(token);
    }
}