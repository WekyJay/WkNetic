package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.ISysUserService;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.dto.UserDTO;
import cn.wekyjay.wknetic.common.model.dto.UserQueryDTO;
import cn.wekyjay.wknetic.common.model.vo.MinecraftProfileVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;




/**
 * 管理员用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final ISysUserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public Result<Page<SysUser>> getUserList(@Valid UserQueryDTO queryDTO) {
        Page<SysUser> page = userService.getUserListByAdmin(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取单个用户详情
     */
    @GetMapping("/{userId}")
    public Result<SysUser> getUserById(@PathVariable Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 隐藏密码
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<String> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            boolean success = userService.createUserByAdmin(userDTO);
            return success ? Result.success("创建用户成功") : Result.error("创建用户失败");
        } catch (RuntimeException e) {
            log.error("创建用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public Result<String> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserDTO userDTO) {
        try {
            userDTO.setUserId(userId);
            boolean success = userService.updateUserByAdmin(userDTO);
            return success ? Result.success("更新用户成功") : Result.error("更新用户失败");
        } catch (RuntimeException e) {
            log.error("更新用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId) {
        try {
            boolean success = userService.deleteUserByAdmin(userId);
            return success ? Result.success("删除用户成功") : Result.error("删除用户失败");
        } catch (RuntimeException e) {
            log.error("删除用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 切换用户状态
     */
    @PatchMapping("/{userId}/status")
    public Result<String> toggleUserStatus(
            @PathVariable Long userId,
            @RequestBody @Validated StatusToggleRequest request) {
        try {
            boolean success = userService.toggleUserStatus(userId, request.getStatus());
            return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
        } catch (RuntimeException e) {
            log.error("状态更新失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 验证 Minecraft UUID（调用Mojang API）
     */
    @GetMapping("/validate-minecraft/{uuid}")
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
     * 状态切换请求
     */
    @Data
    static class StatusToggleRequest {
        @NotNull(message = "状态不能为空")
        private Integer status;
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
