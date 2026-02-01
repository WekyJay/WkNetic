package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.ISysUserService;
import cn.wekyjay.wknetic.admin.system.service.IUserFollowService;
import cn.wekyjay.wknetic.auth.model.LoginUser;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.enums.ResultCode;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.dto.UserProfileUpdateDTO;
import cn.wekyjay.wknetic.common.model.vo.UserInfoVO;
import cn.wekyjay.wknetic.common.model.vo.UserProfileVO;
import cn.wekyjay.wknetic.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 用户相关接口
 */
@Slf4j
@Tag(name = "User Management", description = "User profile and follow operations")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IUserFollowService userFollowService;

    @Autowired
    private JwtUtils jwtUtil;

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "Get Current User Info", description = "Retrieve current login user information including username and roles")
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

            // 查询用户信息（关联角色表获取role_code）
            SysUser user = userService.getUserWithRole(userId);
            
            if (user == null) {
                return Result.error(40401, "用户不存在");
            }

            // 转换为 VO（user.getRole() 已通过JOIN查询得到role_code）
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

    /**
     * 获取用户公开资料
     *
     * @param userId 用户ID
     * @return 用户公开资料
     */
    @Operation(summary = "Get User Profile", description = "Retrieve user public profile information including bio and avatar")
    @Parameters({
            @Parameter(name = "userId", description = "User ID", required = true, example = "1")
    })
    @GetMapping("/profile/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        try {
            Long currentUserId = getCurrentUserId();
            UserProfileVO profile = userService.getUserProfile(userId, currentUserId);
            return Result.success(profile);
        } catch (RuntimeException e) {
            log.error("获取用户资料失败: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 更新个人资料
     *
     * @param profileUpdateDTO 资料更新信息
     * @return 操作结果
     */
    @Operation(summary = "Update User Profile", description = "Update current user profile information")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO profileUpdateDTO) {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(ResultCode.unauthorized.getCode(), "请先登录");
            }

            userService.updateUserProfile(currentUserId, profileUpdateDTO);
            return Result.success();
        } catch (Exception e) {
            log.error("更新个人资料失败: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 关注用户
     *
     * @param userId 被关注者ID
     * @return 操作结果
     */
    @Operation(summary = "Follow User", description = "Current user follows another user to see their activities")
    @Parameters({
            @Parameter(name = "userId", description = "User ID to follow", required = true, example = "2")
    })
    @PostMapping("/{userId}/follow")
    public Result<Void> followUser(@PathVariable Long userId) {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(ResultCode.unauthorized.getCode(), "请先登录");
            }

            userFollowService.followUser(currentUserId, userId);
            return Result.success();
        } catch (RuntimeException e) {
            log.error("关注用户失败: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 取消关注用户
     *
     * @param userId 被取消关注者ID
     * @return 操作结果
     */
    @Operation(summary = "Unfollow User", description = "Current user unfollows another user to stop seeing their activities")
    @Parameters({
            @Parameter(name = "userId", description = "User ID to unfollow", required = true, example = "2")
    })
    @DeleteMapping("/{userId}/follow")
    public Result<Void> unfollowUser(@PathVariable Long userId) {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(ResultCode.unauthorized.getCode(), "请先登录");
            }

            userFollowService.unfollowUser(currentUserId, userId);
            return Result.success();
        } catch (RuntimeException e) {
            log.error("取消关注用户失败: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return loginUser.getUserId();
        }
        return null;
    }
}
