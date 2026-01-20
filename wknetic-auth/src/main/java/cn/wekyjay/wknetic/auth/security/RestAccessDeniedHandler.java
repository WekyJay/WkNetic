package cn.wekyjay.wknetic.auth.security;

import cn.hutool.json.JSONUtil;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.enums.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description:  权限不足处理器
 * @Title: 当用户已登录但权限不足时，自定义返回结果
 * @author WekyJay
 * @Github: <a href="https://github.com/WekyJay">https://github.com/WekyJay</a>
 * @Date: 2026/1/20 13:21
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        
        // 返回标准 JSON: 40300 权限不足
        String json = JSONUtil.toJsonStr(Result.error(ResultCode.FORBIDDEN));
        response.getWriter().println(json);
        response.getWriter().flush();
    }
}