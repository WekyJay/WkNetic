package cn.wekyjay.wknetic.auth.captcha;

/**
 * 验证码验证器接口
 */
public interface CaptchaValidator {
    
    /**
     * 验证验证码
     * 
     * @param token 验证码令牌
     * @param remoteIp 客户端IP地址
     * @return 验证是否通过
     */
    boolean validate(String token, String remoteIp);
    
    /**
     * 获取验证器类型
     * 
     * @return 验证器类型标识
     */
    String getType();
}
