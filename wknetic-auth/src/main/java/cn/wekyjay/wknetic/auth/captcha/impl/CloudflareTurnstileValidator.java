package cn.wekyjay.wknetic.auth.captcha.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.wekyjay.wknetic.auth.captcha.CaptchaValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Cloudflare Turnstile 验证器
 * 文档：https://developers.cloudflare.com/turnstile/
 */
@Slf4j
@Component
public class CloudflareTurnstileValidator implements CaptchaValidator {

    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    @Value("${wknetic.captcha.cloudflare.secret-key:}")
    private String secretKey;

    @Override
    public boolean validate(String token, String remoteIp) {
        if (!StringUtils.hasText(token)) {
            log.warn("Cloudflare Turnstile 令牌为空");
            return false;
        }
        
        if (!StringUtils.hasText(secretKey)) {
            log.error("Cloudflare Turnstile Secret Key 未配置");
            return false;
        }
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("secret", secretKey);
            params.put("response", token);
            if (StringUtils.hasText(remoteIp)) {
                params.put("remoteip", remoteIp);
            }
            
            // 发送验证请求
            String response = HttpUtil.post(VERIFY_URL, params);
            JSONObject json = JSONUtil.parseObj(response);
            
            boolean success = json.getBool("success", false);
            
            if (success) {
                log.info("Cloudflare Turnstile 验证成功");
            } else {
                log.warn("Cloudflare Turnstile 验证失败: {}", json.getStr("error-codes"));
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Cloudflare Turnstile 验证异常", e);
            return false;
        }
    }

    @Override
    public String getType() {
        return "cloudflare";
    }
}
