package cn.wekyjay.wknetic.auth.service.impl;

import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.mapper.SysUserMapper;
import cn.wekyjay.wknetic.auth.model.LoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用");
        }
        
        // 返回 Spring Security 需要的 UserDetails 对象
        return new LoginUser(
                user.getUserId(),
                user.getUsername(),
                user.getPassword()
        );
    }
}