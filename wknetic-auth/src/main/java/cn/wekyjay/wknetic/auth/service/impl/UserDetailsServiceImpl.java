package cn.wekyjay.wknetic.auth.service.impl;

import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.mapper.SysUserMapper;
import cn.wekyjay.wknetic.auth.model.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户（关联角色表获取role_code）
        SysUser user = userMapper.getUserWithRoleByUsername(username);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用");
        }
        
        // 调试日志：查看获取到的角色
        log.info("用户登录: username={}, role={}, roleId={}", username, user.getRole(), user.getRoleId());
        
        // 返回 Spring Security 需要的 UserDetails 对象，包含角色信息
        // user.getRole() 现在来自 sys_role.role_code（通过JOIN查询得到）
        LoginUser loginUser = new LoginUser(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole() // role_code: ADMIN, MODERATOR, USER, VIP, BANNED
        );
        
        log.info("LoginUser 创建成功: authorities={}", loginUser.getAuthorities());
        
        return loginUser;
    }
}