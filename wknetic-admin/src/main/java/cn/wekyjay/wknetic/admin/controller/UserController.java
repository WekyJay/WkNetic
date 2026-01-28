package cn.wekyjay.wknetic.admin.controller;

import cn.wekyjay.wknetic.admin.system.service.ISysUserService;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.vo.UserInfoVO;
import cn.wekyjay.wknetic.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        try {
            // 从请求头获取 token
            String token = getTokenFromRequest(request);
            
            if (token == null) {
                return Result.error(40101, "未登录");
            }

            // 从 token 中解析用户 ID
            Long userId = jwtUtil.getUserId(token);
            
            if (userId == null) {
                return Result.error(40102, "Token 无效");
            }

            // 查询用户信息
            SysUser user = userService.getById(userId);
            
            if (user == null) {
                return Result.error(40401, "用户不存在");
            }

            // 转换为 VO
            UserInfoVO userInfo = new UserInfoVO();
            BeanUtils.copyProperties(user, userInfo);
            
            return Result.success(userInfo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(50000, "获取用户信息失败");
        }
    }

    /**
     * 从请求中获取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
