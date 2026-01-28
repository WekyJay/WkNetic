package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.mapper.SysUserMapper;
import cn.wekyjay.wknetic.admin.system.service.ISysUserService;
import cn.wekyjay.wknetic.common.model.dto.RegisterBody;
import cn.wekyjay.wknetic.common.model.dto.ResetPasswordBody;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户 Service 实现
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean register(RegisterBody registerBody) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, registerBody.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(registerBody.getEmail())) {
            LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(SysUser::getEmail, registerBody.getEmail());
            if (this.count(emailWrapper) > 0) {
                throw new RuntimeException("邮箱已被注册");
            }
        }
        
        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerBody.getUsername());
        user.setPassword(passwordEncoder.encode(registerBody.getPassword()));
        user.setNickname(StringUtils.hasText(registerBody.getNickname()) 
                ? registerBody.getNickname() 
                : registerBody.getUsername());
        user.setEmail(registerBody.getEmail());
        user.setStatus(1); // 默认启用
        
        boolean success = this.save(user);
        
        if (success) {
            log.info("用户注册成功: {}", registerBody.getUsername());
        }
        
        return success;
    }

    @Override
    public boolean resetPassword(ResetPasswordBody resetPasswordBody) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, resetPasswordBody.getUsername());
        
        if (StringUtils.hasText(resetPasswordBody.getEmail())) {
            wrapper.eq(SysUser::getEmail, resetPasswordBody.getEmail());
        }
        
        SysUser user = this.getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在或邮箱不匹配");
        }
        
        // 更新密码
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getUserId, user.getUserId())
                    .set(SysUser::getPassword, passwordEncoder.encode(resetPasswordBody.getNewPassword()));
        
        boolean success = this.update(updateWrapper);
        
        if (success) {
            log.info("用户重置密码成功: {}", resetPasswordBody.getUsername());
        }
        
        return success;
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return this.getOne(wrapper);
    }
}
