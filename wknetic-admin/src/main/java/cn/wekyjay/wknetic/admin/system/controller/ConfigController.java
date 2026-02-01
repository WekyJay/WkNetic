package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.domain.SysConfig;
import cn.wekyjay.wknetic.admin.system.service.ISysConfigService;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ConfigController {

    @Autowired
    private ISysConfigService configService;

    /**
     * 获取站点配置（公开接口，无需登录）
     * 返回站点名称、Logo、SEO信息等
     */
    @GetMapping("/open/site-config")
    public Result<Map<String, String>> getSiteConfig() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigGroup, "site")
               .eq(SysConfig::getStatus, 1);
        
        List<SysConfig> configs = configService.list(wrapper);
        
        Map<String, String> result = configs.stream()
                .collect(Collectors.toMap(
                        SysConfig::getConfigKey,
                        config -> config.getConfigValue() != null ? config.getConfigValue() : "",
                        (v1, v2) -> v1
                ));
        
        return Result.success(result);
    }

    /**
     * 获取所有配置（需要管理员权限）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/config/list")
    public Result<List<SysConfig>> listConfigs() {
        List<SysConfig> configs = configService.list();
        return Result.success(configs);
    }

    /**
     * 根据配置组获取配置（需要管理员权限）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/config/group/{group}")
    public Result<List<SysConfig>> getConfigsByGroup(@PathVariable String group) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigGroup, group)
               .orderByAsc(SysConfig::getSortOrder);
        
        List<SysConfig> configs = configService.list(wrapper);
        return Result.success(configs);
    }

    /**
     * 更新配置（需要管理员权限）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/config/update")
    public Result<String> updateConfig(@Validated @RequestBody SysConfig config) {
        boolean success = configService.updateById(config);
        
        if (success) {
            log.info("配置更新成功: {}", config.getConfigKey());
            return Result.success("配置更新成功");
        } else {
            return Result.error(50000, "配置更新失败");
        }
    }

    /**
     * 批量更新配置（需要管理员权限）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/config/batch-update")
    public Result<String> batchUpdateConfigs(@RequestBody Map<String, String> configMap) {
        try {
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                String configKey = entry.getKey();
                String configValue = entry.getValue();
                
                LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysConfig::getConfigKey, configKey);
                
                SysConfig config = configService.getOne(wrapper);
                if (config != null) {
                    config.setConfigValue(configValue);
                    configService.updateById(config);
                }
            }
            
            log.info("批量更新配置成功，共 {} 项", configMap.size());
            return Result.success("批量更新成功");
        } catch (Exception e) {
            log.error("批量更新配置失败", e);
            return Result.error(50000, "批量更新失败: " + e.getMessage());
        }
    }
}
