package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.domain.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户（关联角色表获取role_code）
     * @param username 用户名
     * @return 用户信息（包含role字段为role_code）
     */
    @Select("SELECT u.*, r.role_code as role FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.role_id " +
            "WHERE u.username = #{username}")
    SysUser getUserWithRoleByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户（关联角色表获取role_code）
     * @param userId 用户ID
     * @return 用户信息（包含role字段为role_code）
     */
    @Select("SELECT u.*, r.role_code as role FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.role_id " +
            "WHERE u.user_id = #{userId}")
    SysUser getUserWithRoleById(@Param("userId") Long userId);
}
