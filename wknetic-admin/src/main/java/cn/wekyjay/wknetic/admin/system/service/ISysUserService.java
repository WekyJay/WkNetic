package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.model.dto.RegisterBody;
import cn.wekyjay.wknetic.common.model.dto.ResetPasswordBody;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
