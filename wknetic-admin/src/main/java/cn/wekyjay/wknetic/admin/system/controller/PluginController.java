package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.IUserPluginService;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.dto.InstallPluginDTO;
import cn.wekyjay.wknetic.common.model.dto.UpdatePluginPermissionsDTO;
import cn.wekyjay.wknetic.common.model.dto.UpdatePluginStatusDTO;
import cn.wekyjay.wknetic.common.model.vo.PluginStatusVO;
import cn.wekyjay.wknetic.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 插件管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/plugins")
public class PluginController {

    @Autowired
    private IUserPluginService userPluginService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户已安装的插件列表
     */
    @GetMapping("/installed")
    public Result<List<PluginStatusVO>> getInstalledPlugins(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<PluginStatusVO> plugins = userPluginService.getInstalledPlugins(userId);
            return Result.success(plugins);
        } catch (Exception e) {
            log.error("获取已安装插件列表失败", e);
            return Result.error(500, "获取插件列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取已启用的插件ID列表
     */
    @GetMapping("/enabled")
    public Result<List<String>> getEnabledPlugins(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<String> pluginIds = userPluginService.getEnabledPluginIds(userId);
            return Result.success(pluginIds);
        } catch (Exception e) {
            log.error("获取已启用插件列表失败", e);
            return Result.error(500, "获取插件列表失败：" + e.getMessage());
        }
    }

    /**
     * 安装插件
     */
    @PostMapping("/install")
    public Result<PluginStatusVO> installPlugin(@Valid @RequestBody InstallPluginDTO dto, 
                                                 HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            PluginStatusVO plugin = userPluginService.installPlugin(userId, dto);
            return Result.success(plugin, "安装成功");
        } catch (RuntimeException e) {
            log.warn("安装插件失败: {}", e.getMessage());
            return Result.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("安装插件失败", e);
            return Result.error(500, "安装失败：" + e.getMessage());
        }
    }

    /**
     * 卸载插件
     */
    @DeleteMapping("/{pluginId}")
    public Result<Void> uninstallPlugin(@PathVariable String pluginId, 
                                        HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            userPluginService.uninstallPlugin(userId, pluginId);
            return Result.success("卸载成功");
        } catch (RuntimeException e) {
            log.warn("卸载插件失败: {}", e.getMessage());
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("卸载插件失败", e);
            return Result.error(500, "卸载失败：" + e.getMessage());
        }
    }

    /**
     * 更新插件状态（启用/禁用）
     */
    @PutMapping("/{pluginId}/status")
    public Result<Void> updatePluginStatus(@PathVariable String pluginId,
                                           @Valid @RequestBody UpdatePluginStatusDTO dto,
                                           HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            userPluginService.updatePluginStatus(userId, pluginId, dto.getEnabled());
            return Result.success("状态更新成功");
        } catch (RuntimeException e) {
            log.warn("更新插件状态失败: {}", e.getMessage());
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("更新插件状态失败", e);
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 获取插件权限
     */
    @GetMapping("/{pluginId}/permissions")
    public Result<List<String>> getPluginPermissions(@PathVariable String pluginId,
                                                     HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<String> permissions = userPluginService.getPluginPermissions(userId, pluginId);
            return Result.success(permissions);
        } catch (RuntimeException e) {
            log.warn("获取插件权限失败: {}", e.getMessage());
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("获取插件权限失败", e);
            return Result.error(500, "获取失败：" + e.getMessage());
        }
    }

    /**
     * 更新插件权限
     */
    @PutMapping("/{pluginId}/permissions")
    public Result<Void> updatePluginPermissions(@PathVariable String pluginId,
                                               @Valid @RequestBody UpdatePluginPermissionsDTO dto,
                                               HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            userPluginService.updatePluginPermissions(userId, pluginId, dto);
            return Result.success("权限更新成功");
        } catch (RuntimeException e) {
            log.warn("更新插件权限失败: {}", e.getMessage());
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("更新插件权限失败", e);
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 从请求中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            throw new RuntimeException("未登录");
        }
        
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new RuntimeException("Token无效");
        }
        
        return userId;
    }

    /**
     * 从请求头中提取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
