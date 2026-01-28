package cn.wekyjay.wknetic.auth.service.impl;


import cn.wekyjay.wknetic.auth.model.LoginUser;
import cn.wekyjay.wknetic.common.model.dto.LoginBody;
import cn.wekyjay.wknetic.auth.service.AuthService;
import cn.wekyjay.wknetic.common.exception.BusinessException;
import cn.wekyjay.wknetic.common.util.JwtUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public String login(LoginBody loginBody) {
        // 1. 构建 UsernamePasswordAuthenticationToken
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginBody.getUsername(), loginBody.getPassword());

        try {
            // 2. 调用 Spring Security 的 AuthenticationManager 进行认证
            // 这步会去调用 UserDetailsServiceImpl.loadUserByUsername
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 3. 认证通过，获取用户信息
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            // 4. 生成 JWT Token（根据 rememberMe 设置过期时间）
            boolean rememberMe = loginBody.getRememberMe() != null && loginBody.getRememberMe();
            String token = jwtUtil.createToken(loginUser.getUserId(), loginUser.getUsername(), rememberMe);
            
            log.info("用户 [{}] 登录成功，生成Token (记住我: {})", loginBody.getUsername(), rememberMe);
            return token;

        } catch (BadCredentialsException e) {
            // 捕获密码错误异常，抛出我们自定义的业务异常
            throw new BusinessException("用户名或密码错误");
        } catch (Exception e) {
            log.error("登录认证失败", e);
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }
}