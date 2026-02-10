package cn.wekyjay.wknetic.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 */
@Component
public class RedisUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 设置键值
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置键值（带过期时间）
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        log.info("Redis set with TTL: key={}, timeout={}, unit={}", key, timeout, unit);
        redisTemplate.opsForValue().set(key, value, timeout, unit);
        Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        log.info("Redis TTL after set: key={}, ttlSeconds={}", key, ttlSeconds);
    }

    /**
     * 获取值
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 原子操作：获取并删除键值
     */
    public String getAndDelete(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            redisTemplate.delete(key);
        }
        return value;
    }

    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
}
