package cn.wekyjay.wknetic.auth.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Captcha Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/captcha")
public class CaptchaController {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 生成简易图形验证码
     * 返回 base64 图片和 sessionId
     */
    @GetMapping("/generate")
    public Result<Map<String, String>> generateCaptcha() {
        // 生成验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String code = captcha.getCode();
        String imageBase64 = captcha.getImageBase64();
        
        // 生成唯一 session ID
        String sessionId = IdUtil.simpleUUID();
        
        // 存储到 Redis，5 分钟过期
        String cacheKey = "captcha:" + sessionId;
        redisUtils.set(cacheKey, code, 5, TimeUnit.MINUTES);
        
        log.debug("生成验证码: sessionId={}, code={}", sessionId, code);
        
        Map<String, String> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("image", "data:image/png;base64," + imageBase64);
        
        return Result.success(result);
    }

    /**
     * 获取验证码图片（直接输出到响应流）
     * 
     * @deprecated 推荐使用 /generate 接口返回 base64
     */
    @GetMapping("/image")
    public void getCaptchaImage(HttpServletResponse response) throws IOException {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String code = captcha.getCode();
        String sessionId = IdUtil.simpleUUID();
        
        String cacheKey = "captcha:" + sessionId;
        redisUtils.set(cacheKey, code, 5, TimeUnit.MINUTES);
        
        // 在响应头返回 sessionId
        response.setHeader("X-Captcha-Session", sessionId);
        
        captcha.write(response.getOutputStream());
    }
}
