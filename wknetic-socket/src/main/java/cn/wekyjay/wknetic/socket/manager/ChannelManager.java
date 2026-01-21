package cn.wekyjay.wknetic.socket.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelManager {

    // Key: Token (或者 ServerID), Value: Channel
    private static final ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    
    // 反向查找表: ChannelId -> Token (用于断线时快速移除)
    private static final ConcurrentHashMap<ChannelId, String> idToTokenMap = new ConcurrentHashMap<>();

    public void addChannel(String token, Channel channel) {
        channelMap.put(token, channel);
        idToTokenMap.put(channel.id(), token);
    }

    public void removeChannel(Channel channel) {
        String token = idToTokenMap.remove(channel.id());
        if (token != null) {
            channelMap.remove(token);
        }
    }

    public Channel getChannel(String token) {
        return channelMap.get(token);
    }
    
    // 获取在线数量
    public int size() {
        return channelMap.size();
    }
}