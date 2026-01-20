package cn.wekyjay.wknetic.admin;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class TestUnit {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedis() {
        // 1. 存数据
        redisTemplate.opsForValue().set("wknetic:hello", "Hello Redis!");
        System.out.println("数据写入成功！");

        // 2. 取数据
        Object value = redisTemplate.opsForValue().get("wknetic:hello");
        System.out.println("数据读取成功: " + value);

        // 3. 验证 JSON 序列化 (存个对象试试)
        // redisTemplate.opsForValue().set("wknetic:user:1", new LoginUser(...));
    }
}