package cn.wekyjay.wknetic.auth.captcha.impl;

import cn.wekyjay.wknetic.auth.captcha.CaptchaValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 无验证码验证器（用于开发环境或特殊场景）
 */
@Slf4j
@Component
public class NoneCaptchaValidator implements CaptchaValidator {

    @Override
    public boolean validate(String token, String remoteIp) {
        log.debug("无验证码模式，直接通过");
        return true;
    }

    @Override
    public String getType() {
        return "none";
    }
}
