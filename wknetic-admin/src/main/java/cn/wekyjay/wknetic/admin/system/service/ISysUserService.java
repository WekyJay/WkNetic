package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.model.dto.RegisterBody;
import cn.wekyjay.wknetic.common.model.dto.ResetPasswordBody;
import cn.wekyjay.wknetic.common.model.dto.UserDTO;
import cn.wekyjay.wknetic.common.model.dto.UserQueryDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 用户 Service 接口
 */
public interface ISysUserService extends IService<SysUser> {
    
    /**
     * 用户注册
     * 
     * @param registerBody 注册信息
     * @return 是否成功
     */
    boolean register(RegisterBody registerBody);
    
    /**
     * 重置密码
     * 
     * @param resetPasswordBody 重置密码信息
     * @return 是否成功
     */
    boolean resetPassword(ResetPasswordBody resetPasswordBody);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserByUsername(String username);
    
    /**
     * 管理员分页查询用户列表
     * 
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<SysUser> getUserListByAdmin(UserQueryDTO queryDTO);
    
    /**
     * 管理员创建用户
     * 
     * @param userDTO 用户信息
     * @return 是否成功
     */
    boolean createUserByAdmin(UserDTO userDTO);
    
    /**
     * 管理员更新用户信息
     * 
     * @param userDTO 用户信息
     * @return 是否成功
     */
    boolean updateUserByAdmin(UserDTO userDTO);
    
    /**
     * 管理员删除用户
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUserByAdmin(Long userId);
    
    /**
     * 切换用户状态
     * 
     * @param userId 用户ID
     * @param status 目标状态
     * @return 是否成功
     */
    boolean toggleUserStatus(Long userId, Integer status);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @param excludeUserId 排除的用户ID（更新时使用）
     * @return 是否存在
     */
    boolean isUsernameExists(String username, Long excludeUserId);
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱
     * @param excludeUserId 排除的用户ID（更新时使用）
     * @return 是否存在
     */
    boolean isEmailExists(String email, Long excludeUserId);
    
    /**
     * 检查Minecraft UUID是否已被绑定
     * 
     * @param minecraftUuid Minecraft UUID
     * @param excludeUserId 排除的用户ID（更新时使用）
     * @return 是否已被绑定
     */
    boolean isMinecraftUuidBound(String minecraftUuid, Long excludeUserId);
}

