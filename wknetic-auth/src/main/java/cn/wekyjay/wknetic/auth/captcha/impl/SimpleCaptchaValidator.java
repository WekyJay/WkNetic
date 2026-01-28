package cn.wekyjay.wknetic.auth.captcha.impl;

import cn.wekyjay.wknetic.auth.captcha.CaptchaValidator;
import cn.wekyjay.wknetic.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 简易验证码验证器
 * 验证码存储在 Redis 中，key 格式：captcha:{sessionId}
 */
@Slf4j
@Component
public class SimpleCaptchaValidator implements CaptchaValidator {

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public boolean validate(String token, String remoteIp) {
        if (!StringUtils.hasText(token)) {
            log.warn("验证码令牌为空");
            return false;
        }
        
        // token 格式：sessionId:code
        String[] parts = token.split(":");
        if (parts.length != 2) {
            log.warn("验证码令牌格式错误: {}", token);
            return false;
        }
        
        String sessionId = parts[0];
        String code = parts[1];
        
        // 从 Redis 获取验证码
        String cacheKey = "captcha:" + sessionId;
        String cachedCode = redisUtils.get(cacheKey);
        
        if (!StringUtils.hasText(cachedCode)) {
            log.warn("验证码不存在或已过期: {}", sessionId);
            return false;
        }
        
        // 验证码不区分大小写
        boolean valid = cachedCode.equalsIgnoreCase(code);
        
        // 验证通过后删除验证码（一次性使用）
        if (valid) {
            redisUtils.delete(cacheKey);
            log.info("验证码验证成功: {}", sessionId);
        } else {
            log.warn("验证码错误: {} != {}", code, cachedCode);
        }
        
        return valid;
    }

    @Override
    public String getType() {
        return "simple";
    }
}
