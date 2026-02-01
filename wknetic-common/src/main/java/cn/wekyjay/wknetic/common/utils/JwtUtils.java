package cn.wekyjay.wknetic.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import cn.wekyjay.wknetic.common.config.JwtProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtils {

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // Redis Key 前缀: auth:token:userId
    private static final String LOGIN_USER_KEY = "auth:token:";

    /**
     * 生成 Token (登录成功后调用)
     * @param userId 用户ID
     * @param username 用户名 (放入 Payload 方便前端解析展示)
     * @param rememberMe 是否记住我（true=30天，false=24小时）
     */
    public String createToken(Long userId, String username, boolean rememberMe) {
        // 1. 构建 Hutool JWT
        Map<String, Object> payload = MapUtil.newHashMap();
        payload.put(JWTPayload.ISSUED_AT, DateUtil.date()); // 签发时间
        payload.put(JWTPayload.SUBJECT, String.valueOf(userId)); // 用户ID
        payload.put("username", username); // 扩展字段

        byte[] key = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        String token = JWTUtil.createToken(payload, key);

        // 2. [关键] 将 Token 存入 Redis，实现有状态管理
        // Key: auth:token:1001  Value: eyJhbGciOi... (Token字符串)
        // 根据 rememberMe 设置不同的过期时间：记住我=30天，否则=24小时
        String redisKey = LOGIN_USER_KEY + userId;
        long expiration = rememberMe ? jwtProperties.getExpiration() : 86400; // 86400秒=24小时
        stringRedisTemplate.opsForValue().set(redisKey, token, expiration, TimeUnit.SECONDS);

        return token;
    }
    
    /**
     * 生成 Token (兼容旧方法，默认24小时)
     * @param userId 用户ID
     * @param username 用户名
     */
    public String createToken(Long userId, String username) {
        return createToken(userId, username, false);
    }

    /**
     * 解析 Token 获取用户 ID
     */
    public Long getUserId(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            return Long.valueOf((String) jwt.getPayload(JWTPayload.SUBJECT));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验 Token 并自动续签 (在拦截器中调用)
     * @return true=有效, false=无效
     */
    public boolean validateAndRenew(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        // 1. 基础格式校验 (验签)
        byte[] key = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        boolean verify = JWTUtil.verify(token, key);
        if (!verify) {
            return false;
        }

        // 2. 获取用户 ID
        Long userId = getUserId(token);
        if (userId == null) {
            return false;
        }

        // 3. [关键] Redis 校验 (检查是否被踢下线、是否过期)
        String redisKey = LOGIN_USER_KEY + userId;
        String storedToken = stringRedisTemplate.opsForValue().get(redisKey);

        // 如果 Redis 里没有，或者 Redis 里的 Token 和传来的不一致 (单点登录被挤掉)，则无效
        if (storedToken == null || !storedToken.equals(token)) {
            return false;
        }

        // 4. [自动续签逻辑] 滑动窗口
        Long expire = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        // 如果剩余时间 < 阈值 (比如剩30分钟)，且用户在操作，则重置为最大过期时间 (30天)
        if (expire != null && expire < jwtProperties.getRefreshTime()) {
            stringRedisTemplate.expire(redisKey, jwtProperties.getExpiration(), TimeUnit.SECONDS);
            // log.debug("用户 {} Token 自动续期成功", userId);
        }

        return true;
    }

    /**
     * 退出登录 (移除 Redis)
     */
    public void logout(Long userId) {
        String redisKey = LOGIN_USER_KEY + userId;
        stringRedisTemplate.delete(redisKey);
    }
}
