package cn.wekyjay.wknetic.auth.captcha;

import cn.wekyjay.wknetic.auth.captcha.impl.CloudflareTurnstileValidator;
import cn.wekyjay.wknetic.auth.captcha.impl.NoneCaptchaValidator;
import cn.wekyjay.wknetic.auth.captcha.impl.SimpleCaptchaValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 验证码验证器工厂
 * 根据配置动态选择验证器
 */
@Slf4j
@Component
public class CaptchaFactory {

    @Autowired
    private SimpleCaptchaValidator simpleCaptchaValidator;

    @Autowired
    private CloudflareTurnstileValidator cloudflareTurnstileValidator;

    @Autowired
    private NoneCaptchaValidator noneCaptchaValidator;

    /**
     * 根据类型获取验证器
     * 
     * @param type 验证器类型：simple, cloudflare, none
     * @return 验证器实例
     */
    public CaptchaValidator getValidator(String type) {
        if (type == null || type.isEmpty()) {
            type = "simple"; // 默认使用简易验证码
        }
        
        switch (type.toLowerCase()) {
            case "simple":
                return simpleCaptchaValidator;
            case "cloudflare":
                return cloudflareTurnstileValidator;
            case "none":
                return noneCaptchaValidator;
            default:
                log.warn("未知的验证码类型: {}, 使用默认的 simple 验证器", type);
                return simpleCaptchaValidator;
        }
    }
}
