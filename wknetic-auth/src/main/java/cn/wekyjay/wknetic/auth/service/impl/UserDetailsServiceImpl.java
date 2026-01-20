package cn.wekyjay.wknetic.auth.service.impl;

import cn.wekyjay.wknetic.auth.model.LoginUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 以后在这里注入 UserMapper 查数据库
        // User user = userMapper.selectByUsername(username);
        
        // 2. 目前先模拟一个假用户，让你先把流程跑通
        // 这里的 User 必须是 cn.wekyjay.wknetic.auth.model.LoginUser
        // 否则 AuthServiceImpl 强转会报 ClassCastException
        return new LoginUser(
                1L, // 临时 ID
                username,
                new BCryptPasswordEncoder().encode("123456") // 临时密码
        );
    }
}