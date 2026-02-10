package cn.wekyjay.wknetic.admin;

import cn.wekyjay.wknetic.common.utils.RedisUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = WkNeticSpringApplication.class)
@ActiveProfiles("dev")
class RedisUtilsIntegrationTest {

    private static final String KEY = "test:redis-utils:ttl2";

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // @AfterEach
    // void cleanup() {
    //     redisTemplate.delete(KEY);
    // }

    @Test
    void setWithTtlPersistsToRedis() {
        redisUtils.set(KEY, "v1", 20, TimeUnit.SECONDS);

        String value = redisUtils.get(KEY);
        Long ttlSeconds = redisTemplate.getExpire(KEY, TimeUnit.SECONDS);

        assertEquals("v1", value);
        assertNotNull(ttlSeconds);
        assertTrue(ttlSeconds > 0 && ttlSeconds <= 20);
    }
}
