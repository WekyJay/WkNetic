package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.ISysUserService;
import cn.wekyjay.wknetic.admin.system.service.IUserFollowService;
import cn.wekyjay.wknetic.auth.model.LoginUser;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.enums.ResultCode;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.dto.MinecraftBindDTO;
import cn.wekyjay.wknetic.common.model.dto.UserProfileUpdateDTO;
import cn.wekyjay.wknetic.common.model.vo.MinecraftBindingInfo;
import cn.wekyjay.wknetic.common.model.vo.MinecraftProfileVO;
import cn.wekyjay.wknetic.common.model.vo.UserInfoVO;
import cn.wekyjay.wknetic.common.model.vo.UserProfileVO;
import cn.wekyjay.wknetic.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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
    
    private final RestTemplate restTemplate = new RestTemplate();

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
     * 验证 Minecraft UUID（调用Mojang API）
     */
    @Operation(summary = "Validate Minecraft UUID", description = "Verify Minecraft UUID using Mojang Session Server API")
    @Parameters({
            @Parameter(name = "uuid", description = "Minecraft UUID (with or without dashes)", required = true, example = "069a79f4-44e9-4726-a5be-fca90e38aaf5")
    })
    @GetMapping("/minecraft/validate/{uuid}")
    public Result<MinecraftProfileVO> validateMinecraftUuid(@PathVariable String uuid) {
        try {
            // 移除UUID中的破折号
            String cleanUuid = uuid.replace("-", "");
            
            // 调用Mojang API
            String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + cleanUuid;
            MojangProfile profile = restTemplate.getForObject(url, MojangProfile.class);
            
            if (profile != null && profile.getId() != null) {
                MinecraftProfileVO vo = new MinecraftProfileVO();
                vo.setValid(true);
                vo.setId(formatUuid(profile.getId()));
                vo.setName(profile.getName());
                return Result.success(vo);
            } else {
                MinecraftProfileVO vo = new MinecraftProfileVO();
                vo.setValid(false);
                vo.setError("无效的Minecraft UUID");
                return Result.success(vo);
            }
        } catch (Exception e) {
            log.error("验证Minecraft UUID失败: {}", uuid, e);
            MinecraftProfileVO vo = new MinecraftProfileVO();
            vo.setValid(false);
            vo.setError("验证失败：" + e.getMessage());
            return Result.success(vo);
        }
    }

    /**
     * 绑定Minecraft账号（当前登录用户）
     */
    @Operation(summary = "Bind Minecraft Account", description = "Bind Minecraft account to current user")
    @PostMapping("/minecraft/bind")
    public Result<String> bindMinecraftAccount(@Valid @RequestBody MinecraftBindDTO bindDTO) {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(ResultCode.unauthorized.getCode(), "请先登录");
            }
            
            boolean success = userService.bindMinecraftAccount(
                currentUserId, 
                bindDTO.getMinecraftUuid(), 
                bindDTO.getMinecraftUsername()
            );
            return success ? Result.success("Minecraft账号绑定成功") : Result.error("绑定失败");
        } catch (RuntimeException e) {
            log.error("绑定Minecraft账号失败: userId={}", getCurrentUserId(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解绑Minecraft账号（当前登录用户）
     */
    @Operation(summary = "Unbind Minecraft Account", description = "Unbind Minecraft account from current user")
    @DeleteMapping("/minecraft/unbind")
    public Result<String> unbindMinecraftAccount() {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(ResultCode.unauthorized.getCode(), "请先登录");
            }
            
            boolean success = userService.unbindMinecraftAccount(currentUserId);
            return success ? Result.success("Minecraft账号解绑成功") : Result.error("解绑失败");
        } catch (RuntimeException e) {
            log.error("解绑Minecraft账号失败: userId={}", getCurrentUserId(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户的Minecraft绑定信息
     */
    @Operation(summary = "Get Minecraft Binding Info", description = "Get Minecraft binding information for current user")
    @GetMapping("/minecraft/binding-info")
    public Result<MinecraftBindingInfo> getMinecraftBindingInfo() {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(ResultCode.unauthorized.getCode(), "请先登录");
            }
            
            MinecraftBindingInfo info = userService.getMinecraftBindingInfo(currentUserId);
            return Result.success(info);
        } catch (RuntimeException e) {
            log.error("获取Minecraft绑定信息失败: userId={}", getCurrentUserId(), e);
            return Result.error(e.getMessage());
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

    /**
     * 格式化UUID（添加破折号）
     */
    private String formatUuid(String uuid) {
        if (uuid.length() != 32) {
            return uuid;
        }
        return uuid.substring(0, 8) + "-" 
             + uuid.substring(8, 12) + "-" 
             + uuid.substring(12, 16) + "-" 
             + uuid.substring(16, 20) + "-" 
             + uuid.substring(20);
    }

    /**
     * Mojang API 响应
     */
    @Data
    static class MojangProfile {
        private String id;
        private String name;
    }
}
