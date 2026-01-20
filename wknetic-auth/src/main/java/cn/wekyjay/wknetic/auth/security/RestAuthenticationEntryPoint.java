package cn.wekyjay.wknetic.auth.security;

import cn.hutool.json.JSONUtil;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.enums.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description:  当未登录或者 Token 失效访问接口时，自定义返回结果
 * @Title: 鉴权失败处理器
 * @author WekyJay
 * @Github: <a href="https://github.com/WekyJay">https://github.com/WekyJay</a>
 * @Date: 2026/1/20 13:20
 */
@Slf4j
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.warn("用户未登录访问受限接口: {}", request.getRequestURI());
        
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        
        // 返回标准 JSON: 40101 Token无效或已过期
        String json = JSONUtil.toJsonStr(Result.error(ResultCode.TOKEN_INVALID));
        response.getWriter().println(json);
        response.getWriter().flush();
    }
}