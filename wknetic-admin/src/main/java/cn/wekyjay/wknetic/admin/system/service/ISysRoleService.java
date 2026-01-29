package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.admin.system.domain.SysRole;
import cn.wekyjay.wknetic.common.model.dto.RoleDTO;
import cn.wekyjay.wknetic.common.model.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ISysRoleService extends IService<SysRole> {
    
    List<RoleVO> getAllRoles();
    
    RoleVO getRoleById(Long roleId);
    
    boolean createRole(RoleDTO dto);
    
    boolean updateRole(RoleDTO dto);
    
    boolean deleteRole(Long roleId);
    
    SysRole getDefaultRole();
    
    String getDefaultRoleCode();
}
