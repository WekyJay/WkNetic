package cn.wekyjay.wknetic.admin;

import cn.wekyjay.wknetic.common.utils.MessageUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class TestUnit {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void test() {
        String s = MessageUtils.get("code.forbidden");
        System.out.println("s = " + s);
    }
}