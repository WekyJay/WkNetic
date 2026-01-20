package cn.wekyjay.wknetic.auth.security;

import cn.hutool.core.util.StrUtil;
import cn.wekyjay.wknetic.common.config.JwtProperties;
import cn.wekyjay.wknetic.common.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


/**
 * @Description:
 * @Title: JWT登录授权过滤器
 * @author WekyJay
 * @Github: <a href="https://github.com/WekyJay">https://github.com/WekyJay</a>
 * @Date: 2026/1/20 13:22
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 1. 获取请求头中的 Token
        String header = request.getHeader(jwtProperties.getHeader());
        
        // 如果 header 为空或不以 Bearer 开头，直接放行 (交给后面的 Security 拦截)
        // 为什么不报错？因为有的接口是允许匿名访问的 (如 /login)，这里不能把路堵死
        if (StrUtil.isBlank(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 截取 "Bearer " 之后的部分
        String authToken = header.substring(7);

        // 2. 校验 Token 并在 Redis 中自动续签 (核心逻辑)
        if (SecurityContextHolder.getContext().getAuthentication() == null && jwtUtil.validateAndRenew(authToken)) {
            
            Long userId = jwtUtil.getUserId(authToken);
            log.debug("JWT 过滤器通过，用户ID: {}", userId);

            // 3. 构建 Authentication 对象
            // TODO: 后续在这里注入 UserDetailsService 获取用户具体的 Role 权限
            // 目前先给一个空权限列表，保证能登录即可
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.emptyList());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 4. 将用户信息存入 Security 上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
}