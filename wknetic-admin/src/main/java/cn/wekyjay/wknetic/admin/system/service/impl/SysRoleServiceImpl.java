package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.admin.system.domain.SysRole;
import cn.wekyjay.wknetic.admin.system.mapper.SysRoleMapper;
import cn.wekyjay.wknetic.admin.system.service.ISysRoleService;
import cn.wekyjay.wknetic.common.model.dto.RoleDTO;
import cn.wekyjay.wknetic.common.model.vo.RoleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Override
    public List<RoleVO> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysRole::getSortOrder).orderByAsc(SysRole::getRoleId);
        
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleById(Long roleId) {
        SysRole role = this.getById(roleId);
        return role != null ? convertToVO(role) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(RoleDTO dto) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("角色编码已存在");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        
        // 如果设置为默认角色，需要取消其他角色的默认状态
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultRole();
        }
        
        return this.save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleDTO dto) {
        if (dto.getRoleId() == null) {
            throw new RuntimeException("角色ID不能为空");
        }

        SysRole role = this.getById(dto.getRoleId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 检查角色编码是否与其他角色重复
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode())
               .ne(SysRole::getRoleId, dto.getRoleId());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("角色编码已存在");
        }

        BeanUtils.copyProperties(dto, role);
        
        // 如果设置为默认角色，需要取消其他角色的默认状态
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultRole();
        }
        
        return this.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 不允许删除默认角色
        if (Boolean.TRUE.equals(role.getIsDefault())) {
            throw new RuntimeException("不能删除默认角色");
        }

        // TODO: 检查是否有用户使用此角色
        
        return this.removeById(roleId);
    }

    @Override
    public SysRole getDefaultRole() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getIsDefault, true)
               .eq(SysRole::getStatus, 1)
               .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public String getDefaultRoleCode() {
        SysRole defaultRole = getDefaultRole();
        return defaultRole != null ? defaultRole.getRoleCode() : "USER";
    }

    /**
     * 清除所有角色的默认状态
     */
    private void clearDefaultRole() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getIsDefault, true);
        
        List<SysRole> defaultRoles = this.list(wrapper);
        for (SysRole role : defaultRoles) {
            role.setIsDefault(false);
            this.updateById(role);
        }
    }

    private RoleVO convertToVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
