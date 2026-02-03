package cn.wekyjay.wknetic.admin.config;

import cn.wekyjay.wknetic.admin.listener.ServerStatusListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import jakarta.annotation.Resource;

/**
 * Redis监听器配置 - Admin模块
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Configuration
public class AdminRedisListenerConfig {

    @Resource
    private ServerStatusListener serverStatusListener;

    @Bean
    public RedisMessageListenerContainer adminRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(
                new MessageListenerAdapter(serverStatusListener),
                new PatternTopic("wknetic:server:status:*")
        );

        return container;
    }
}
