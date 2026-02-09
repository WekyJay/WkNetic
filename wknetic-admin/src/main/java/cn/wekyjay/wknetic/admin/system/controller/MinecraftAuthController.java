package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.MinecraftAuthService;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.dto.DeviceFlowStateDTO;
import cn.wekyjay.wknetic.common.model.vo.MinecraftProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Minecraft认证控制器
 * 使用MinecraftAuth库简化Microsoft OAuth流程
 * 支持异步设备流认证和轮询
 */
@Slf4j
@Tag(name = "Minecraft Auth", description = "Minecraft认证相关接口（使用MinecraftAuth库）")
@RestController
@RequestMapping("/api/v1/minecraft-auth")
@RequiredArgsConstructor
public class MinecraftAuthController {

    private final MinecraftAuthService minecraftAuthService;

    /**
     * 启动Minecraft设备流认证
     */
    @Operation(summary = "Start Minecraft Device Flow", description = "启动Minecraft设备流认证（使用MinecraftAuth库）")
    @PostMapping("/device-flow/start")
    public Result<Map<String, Object>> startMinecraftDeviceFlow() {
        try {
            if (!minecraftAuthService.isMicrosoftOAuthEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }

            Map<String, Object> result = minecraftAuthService.startDeviceFlowAsync();
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("启动Minecraft设备流认证失败", e);
            return Result.error("启动Minecraft设备流认证失败: " + e.getMessage());
        }
    }

    /**
     * 轮询设备流状态
     */
    @Operation(summary = "Poll Device Flow Status", description = "轮询设备流认证状态")
    @GetMapping("/device-flow/poll/{deviceCode}")
    public Result<DeviceFlowStateDTO> pollDeviceFlowStatus(@PathVariable String deviceCode) {
        try {
            if (!minecraftAuthService.isMicrosoftOAuthEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }

            DeviceFlowStateDTO state = minecraftAuthService.pollDeviceFlowStatus(deviceCode);
            
            if (state == null) {
                return Result.error("设备流状态不存在或已过期");
            }
            
            return Result.success(state);
            
        } catch (Exception e) {
            log.error("轮询设备流状态失败: deviceCode={}", deviceCode, e);
            return Result.error("轮询设备流状态失败: " + e.getMessage());
        }
    }

    /**
     * 启动带用户ID的设备流认证
     */
    @Operation(summary = "Start Device Flow with User ID", description = "启动设备流认证并关联用户ID")
    @PostMapping("/device-flow/start-with-user/{userId}")
    public Result<Map<String, Object>> startMinecraftDeviceFlowWithUser(@PathVariable Long userId) {
        try {
            if (!minecraftAuthService.isMicrosoftOAuthEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }

            Map<String, Object> result = minecraftAuthService.startDeviceFlowAsync();
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("启动Minecraft设备流认证失败，用户ID: {}", userId, e);
            return Result.error("启动Minecraft设备流认证失败: " + e.getMessage());
        }
    }

    /**
     * 验证Minecraft UUID
     */
    @Operation(summary = "Validate Minecraft UUID", description = "验证Minecraft UUID格式和有效性")
    @GetMapping("/validate-uuid/{uuid}")
    public Result<MinecraftProfileVO> validateMinecraftUuid(@PathVariable String uuid) {
        try {
            MinecraftProfileVO result = minecraftAuthService.validateMinecraftUuid(uuid);
            if (Boolean.TRUE.equals(result.getValid())) {
                return Result.success(result);
            } else {
                return Result.error(result.getError());
            }
        } catch (Exception e) {
            log.error("验证Minecraft UUID失败: {}", uuid, e);
            return Result.error("验证Minecraft UUID失败: " + e.getMessage());
        }
    }

    /**
     * 使用Microsoft令牌绑定Minecraft账户
     */
    @Operation(summary = "Bind Minecraft with Microsoft Token", description = "使用Microsoft OAuth令牌绑定Minecraft账户")
    @PostMapping("/bind-with-microsoft")
    public Result<Map<String, Object>> bindMinecraftWithMicrosoftToken(@RequestBody Map<String, String> requestBody) {
        try {
            if (!minecraftAuthService.isMicrosoftOAuthEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }

            String accessToken = requestBody.get("access_token");
            String userIdStr = requestBody.get("user_id");
            
            if (accessToken == null || accessToken.isEmpty()) {
                return Result.error("访问令牌不能为空");
            }
            
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            
            Long userId = Long.parseLong(userIdStr);
            Map<String, Object> result = minecraftAuthService.bindMinecraftWithMicrosoftToken(accessToken, userId);
            
            return Result.success(result);
            
        } catch (NumberFormatException e) {
            log.error("用户ID格式不正确", e);
            return Result.error("用户ID格式不正确");
        } catch (Exception e) {
            log.error("绑定Minecraft账户失败", e);
            return Result.error("绑定Minecraft账户失败: " + e.getMessage());
        }
    }

    /**
     * 获取Microsoft OAuth配置信息
     */
    @Operation(summary = "Get Microsoft OAuth Config", description = "获取Microsoft OAuth配置信息")
    @GetMapping("/config")
    public Result<Map<String, Object>> getMicrosoftConfig() {
        try {
            Map<String, Object> config = minecraftAuthService.getMicrosoftOAuthConfig();
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取Microsoft OAuth配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 测试MinecraftAuth库连接
     */
    @Operation(summary = "Test MinecraftAuth Connection", description = "测试MinecraftAuth库连接和配置")
    @GetMapping("/test-connection")
    public Result<Map<String, Object>> testConnection() {
        try {
            Map<String, Object> result = minecraftAuthService.testConnection();
            
            if ("success".equals(result.get("status"))) {
                return Result.success(result);
            } else {
                return Result.error((String) result.get("message"));
            }
            
        } catch (Exception e) {
            log.error("测试MinecraftAuth库连接失败", e);
            return Result.error("测试连接失败: " + e.getMessage());
        }
    }

    /**
     * 检查Microsoft OAuth功能状态
     */
    @Operation(summary = "Check Microsoft OAuth Status", description = "检查Microsoft OAuth功能是否启用")
    @GetMapping("/status")
    public Result<Map<String, Object>> checkStatus() {
        try {
            boolean enabled = minecraftAuthService.isMicrosoftOAuthEnabled();
            
            Map<String, Object> result = Map.of(
                "enabled", enabled,
                "message", enabled ? "Microsoft OAuth功能已启用" : "Microsoft OAuth功能未启用",
                "timestamp", System.currentTimeMillis()
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查Microsoft OAuth状态失败", e);
            return Result.error("检查状态失败: " + e.getMessage());
        }
    }
}