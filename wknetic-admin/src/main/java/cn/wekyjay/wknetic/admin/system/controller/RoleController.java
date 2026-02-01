package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.system.service.ISysRoleService;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.dto.RoleDTO;
import cn.wekyjay.wknetic.common.model.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Slf4j
@Tag(name = "Role Management", description = "System role management (ADMIN role required)")
@RestController
@RequestMapping("/api/v1/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    @Autowired
    private ISysRoleService roleService;

    @Operation(summary = "Get All Roles", description = "Retrieve list of all system roles")
    @GetMapping("/list")
    public Result<List<RoleVO>> getAllRoles() {
        try {
            List<RoleVO> roles = roleService.getAllRoles();
            return Result.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.error(500, "获取角色列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "Get Role By ID", description = "Retrieve specific role details")
    @Parameters({
            @Parameter(name = "roleId", description = "Role ID", required = true, example = "1")
    })
    @GetMapping("/{roleId}")
    public Result<RoleVO> getRoleById(@PathVariable Long roleId) {
        try {
            RoleVO role = roleService.getRoleById(roleId);
            if (role == null) {
                return Result.error(404, "角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            log.error("获取角色详情失败", e);
            return Result.error(500, "获取角色详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "Create Role", description = "Create a new system role")
    @PostMapping("/create")
    public Result<Void> createRole(@Valid @RequestBody RoleDTO dto) {
        try {
            roleService.createRole(dto);
            return Result.success("创建成功");
        } catch (RuntimeException e) {
            log.warn("创建角色失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return Result.error(500, "创建失败：" + e.getMessage());
        }
    }

    @Operation(summary = "Update Role", description = "Update existing system role")
    @PutMapping("/update")
    public Result<Void> updateRole(@Valid @RequestBody RoleDTO dto) {
        try {
            roleService.updateRole(dto);
            return Result.success("更新成功");
        } catch (RuntimeException e) {
            log.warn("更新角色失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }

    @Operation(summary = "Delete Role", description = "Delete a system role")
    @Parameters({
            @Parameter(name = "roleId", description = "Role ID to delete", required = true, example = "1")
    })
    @DeleteMapping("/{roleId}")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        try {
            roleService.deleteRole(roleId);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            log.warn("删除角色失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    @Operation(summary = "Get Default Role", description = "Get the default role code assigned to new users")
    @GetMapping("/default")
    public Result<String> getDefaultRoleCode() {
        try {
            String roleCode = roleService.getDefaultRoleCode();
            return Result.success(roleCode);
        } catch (Exception e) {
            log.error("获取默认角色失败", e);
            return Result.error(500, "获取默认角色失败：" + e.getMessage());
        }
    }
}
