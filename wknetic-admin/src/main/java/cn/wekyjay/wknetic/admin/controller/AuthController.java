package cn.wekyjay.wknetic.admin.controller;

import cn.wekyjay.wknetic.auth.model.dto.LoginBody;
import cn.wekyjay.wknetic.auth.service.AuthService;
import cn.wekyjay.wknetic.common.model.Result;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Validated @RequestBody LoginBody loginBody) {
        // 执行登录
        String token = authService.login(loginBody);
        
        // 封装返回
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        
        // 还可以返回 tokenHead: "Bearer " 给前端方便拼接
        return Result.success(data);
    }
}