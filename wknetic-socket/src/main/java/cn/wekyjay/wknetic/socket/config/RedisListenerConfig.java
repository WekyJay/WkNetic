package cn.wekyjay.wknetic.socket.config;

import cn.wekyjay.wknetic.socket.listener.AdminCommandListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import jakarta.annotation.Resource;

/**
 * Redis配置 - Socket模块
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Configuration
public class RedisListenerConfig {

    @Resource
    private AdminCommandListener adminCommandListener;

    /**
     * Redis消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 订阅管理员命令主题
        container.addMessageListener(
                new MessageListenerAdapter(adminCommandListener),
                new PatternTopic(AdminCommandListener.ADMIN_COMMAND_TOPIC)
        );

        return container;
    }
}
