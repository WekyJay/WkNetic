package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.ISysServerTokenService;
import cn.wekyjay.wknetic.common.domain.SysServerToken;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 服务器Token管理控制器
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/server-token")
@RequiredArgsConstructor
@Tag(name = "服务器Token管理", description = "管理游戏服务器接入Token")
public class ServerTokenController {

    private final ISysServerTokenService serverTokenService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "获取Token列表", description = "分页查询服务器Token列表")
    public Result<Page<SysServerToken>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Token名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<SysServerToken> pageParam = new Page<>(page, size);
        Page<SysServerToken> result = serverTokenService.getTokenPage(pageParam, name, status);
        return Result.success(result);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建Token", description = "创建新的服务器Token")
    public Result<SysServerToken> create(@Valid @RequestBody CreateTokenRequest request) {
        String currentUser = getCurrentUsername();
        SysServerToken token = serverTokenService.createToken(
                request.getName(), 
                request.getRemark(), 
                currentUser
        );
        return Result.success(token);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新Token", description = "更新Token名称和备注")
    public Result<Void> update(
            @Parameter(description = "Token ID") @PathVariable Long id,
            @Valid @RequestBody UpdateTokenRequest request) {
        
        SysServerToken token = serverTokenService.getById(id);
        if (token == null) {
            return Result.error("Token不存在");
        }
        
        token.setName(request.getName());
        token.setRemark(request.getRemark());
        serverTokenService.updateById(token);
        
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除Token", description = "删除指定的服务器Token")
    public Result<Void> delete(@Parameter(description = "Token ID") @PathVariable Long id) {
        serverTokenService.removeById(id);
        return Result.success();
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新Token状态", description = "启用或禁用Token")
    public Result<Void> updateStatus(
            @Parameter(description = "Token ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        
        boolean success = serverTokenService.updateStatus(id, status);
        return success ? Result.success() : Result.error("更新失败");
    }

    @PostMapping("/regenerate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重新生成Token", description = "为指定Token生成新的Token值")
    public Result<String> regenerate(@Parameter(description = "Token ID") @PathVariable Long id) {
        String newToken = serverTokenService.regenerateToken(id);
        return Result.success(newToken);
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    @Data
    public static class CreateTokenRequest {
        @NotBlank(message = "Token名称不能为空")
        private String name;
        private String remark;
    }

    @Data
    public static class UpdateTokenRequest {
        @NotBlank(message = "Token名称不能为空")
        private String name;
        private String remark;
    }
}
