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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    @Autowired
    private ISysRoleService roleService;

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
