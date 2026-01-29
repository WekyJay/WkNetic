package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.ISysConfigService;
import cn.wekyjay.wknetic.admin.system.service.ISysUserService;
import cn.wekyjay.wknetic.auth.captcha.CaptchaFactory;
import cn.wekyjay.wknetic.auth.captcha.CaptchaValidator;
import cn.wekyjay.wknetic.auth.service.AuthService;
import cn.wekyjay.wknetic.common.model.dto.LoginBody;
import cn.wekyjay.wknetic.common.model.dto.RegisterBody;
import cn.wekyjay.wknetic.common.model.dto.ResetPasswordBody;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.vo.LoginResultVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private CaptchaFactory captchaFactory;

    @PostMapping("/login")
    public Result<LoginResultVO> login(@Validated @RequestBody LoginBody loginBody, HttpServletRequest request) {
        // 1. 验证码验证
        if (!validateCaptcha(loginBody.getCaptchaToken(), request)) {
            return Result.error(40001, "验证码验证失败");
        }
        
        // 2. 执行登录
        String token = authService.login(loginBody);
        
        // 3. 计算过期时间（根据 rememberMe）
        boolean rememberMe = loginBody.getRememberMe() != null && loginBody.getRememberMe();
        long expirationSeconds = rememberMe ? 2592000 : 86400; // 30天或24小时
        long expiresAt = System.currentTimeMillis() + (expirationSeconds * 1000);
        
        // 4. 封装返回
        LoginResultVO data = new LoginResultVO(token, expiresAt);
        return Result.success(data);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody RegisterBody registerBody, HttpServletRequest request) {
        // 1. 验证码验证
        if (!validateCaptcha(registerBody.getCaptchaToken(), request)) {
            return Result.error(40001, "验证码验证失败");
        }
        
        // 2. 邮箱验证码验证（预留，暂不实现）
        // if (StringUtils.hasText(registerBody.getEmailCode())) {
        //     // 验证邮箱验证码
        // }
        
        try {
            // 3. 执行注册
            boolean success = userService.register(registerBody);
            
            if (success) {
                return Result.success("注册成功");
            } else {
                return Result.error(50000, "注册失败");
            }
        } catch (RuntimeException e) {
            log.warn("注册失败: {}", e.getMessage());
            return Result.error(40001, e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@Validated @RequestBody ResetPasswordBody resetPasswordBody, HttpServletRequest request) {
        // 1. 验证码验证
        if (!validateCaptcha(resetPasswordBody.getCaptchaToken(), request)) {
            return Result.error(40001, "验证码验证失败");
        }
        
        // 2. 邮箱验证码验证（预留，未来用于验证用户身份）
        // if (StringUtils.hasText(resetPasswordBody.getEmailCode())) {
        //     // 验证邮箱验证码
        // }
        
        try {
            // 3. 执行重置密码
            boolean success = userService.resetPassword(resetPasswordBody);
            
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.error(50000, "密码重置失败");
            }
        } catch (RuntimeException e) {
            log.warn("密码重置失败: {}", e.getMessage());
            return Result.error(40001, e.getMessage());
        }
    }

    /**
     * 验证码验证（通用方法）
     */
    private boolean validateCaptcha(String captchaToken, HttpServletRequest request) {
        // 获取配置的验证码类型
        String captchaType = configService.getConfigValue("security.captcha.type", "simple");
        
        // 获取客户端 IP
        String remoteIp = getRemoteIp(request);
        
        // 获取验证器并验证
        CaptchaValidator validator = captchaFactory.getValidator(captchaType);
        return validator.validate(captchaToken, remoteIp);
    }

    /**
     * 获取客户端真实 IP
     */
    private String getRemoteIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}