package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.enums.UserRole;
import cn.wekyjay.wknetic.common.mapper.SysUserMapper;
import cn.wekyjay.wknetic.admin.system.domain.SysRole;
import cn.wekyjay.wknetic.admin.system.service.ISysRoleService;
import cn.wekyjay.wknetic.admin.system.service.ISysUserService;
import cn.wekyjay.wknetic.admin.system.service.IUserFollowService;
import cn.wekyjay.wknetic.common.model.dto.RegisterBody;
import cn.wekyjay.wknetic.common.model.dto.ResetPasswordBody;
import cn.wekyjay.wknetic.common.model.dto.UserDTO;
import cn.wekyjay.wknetic.common.model.dto.UserQueryDTO;
import cn.wekyjay.wknetic.common.model.dto.UserProfileUpdateDTO;
import cn.wekyjay.wknetic.common.model.vo.MinecraftBindingInfo;
import cn.wekyjay.wknetic.common.model.vo.UserProfileVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private IUserFollowService userFollowService;

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
        
        // 自动分配默认角色ID
        SysRole defaultRole = roleService.getDefaultRole();
        if (defaultRole != null) {
            user.setRoleId(defaultRole.getRoleId());
        }
        
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

    @Override
    public SysUser getUserWithRole(Long userId) {
        return this.baseMapper.getUserWithRoleById(userId);
    }

    @Override
    public Page<SysUser> getUserListByAdmin(UserQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索：用户名、邮箱、MC UUID
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                .like(SysUser::getUsername, queryDTO.getKeyword())
                .or().like(SysUser::getEmail, queryDTO.getKeyword())
                .or().like(SysUser::getMinecraftUuid, queryDTO.getKeyword())
                .or().like(SysUser::getMinecraftUsername, queryDTO.getKeyword())
            );
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }
        
        // 角色筛选
        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(SysUser::getRole, queryDTO.getRole());
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(SysUser::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public boolean createUserByAdmin(UserDTO userDTO) {
        // 检查用户名是否存在
        if (isUsernameExists(userDTO.getUsername(), null)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(userDTO.getEmail()) && isEmailExists(userDTO.getEmail(), null)) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        // 检查MC UUID是否已绑定
        if (StringUtils.hasText(userDTO.getMinecraftUuid()) && 
            isMinecraftUuidBound(userDTO.getMinecraftUuid(), null)) {
            throw new RuntimeException("该Minecraft账号已被绑定");
        }
        
        // 创建用户
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        
        // 加密密码
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            throw new RuntimeException("密码不能为空");
        }
        
        // 设置默认值
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(user.getUsername());
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getRoleId() == null) {
            SysRole defaultRole = roleService.getDefaultRole();
            if (defaultRole != null) {
                user.setRoleId(defaultRole.getRoleId());
            }
        }
        
        boolean success = this.save(user);
        
        if (success) {
            log.info("管理员创建用户成功: {}", user.getUsername());
        }
        
        return success;
    }

    @Override
    public boolean updateUserByAdmin(UserDTO userDTO) {
        if (userDTO.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        // 检查用户是否存在
        SysUser existUser = this.getById(userDTO.getUserId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户名是否被占用
        if (StringUtils.hasText(userDTO.getUsername()) && 
            !userDTO.getUsername().equals(existUser.getUsername()) &&
            isUsernameExists(userDTO.getUsername(), userDTO.getUserId())) {
            throw new RuntimeException("用户名已被占用");
        }
        
        // 检查邮箱是否被占用
        if (StringUtils.hasText(userDTO.getEmail()) && 
            !userDTO.getEmail().equals(existUser.getEmail()) &&
            isEmailExists(userDTO.getEmail(), userDTO.getUserId())) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        // 检查MC UUID是否已绑定
        if (StringUtils.hasText(userDTO.getMinecraftUuid()) &&
            !userDTO.getMinecraftUuid().equals(existUser.getMinecraftUuid()) &&
            isMinecraftUuidBound(userDTO.getMinecraftUuid(), userDTO.getUserId())) {
            throw new RuntimeException("该Minecraft账号已被绑定");
        }
        
        // 更新用户信息
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        
        // 如果提供了新密码，则加密
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }
        
        boolean success = this.updateById(user);
        
        if (success) {
            log.info("管理员更新用户成功: {}", user.getUsername());
        }
        
        return success;
    }

    @Override
    public boolean deleteUserByAdmin(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        boolean success = this.removeById(userId);
        
        if (success) {
            log.info("管理员删除用户成功: {}", user.getUsername());
        }
        
        return success;
    }

    @Override
    public boolean toggleUserStatus(Long userId, Integer status) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getUserId, userId)
               .set(SysUser::getStatus, status);
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("切换用户状态成功: userId={}, status={}", userId, status);
        }
        
        return success;
    }

    @Override
    public boolean isUsernameExists(String username, Long excludeUserId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getUserId, excludeUserId);
        }
        
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean isEmailExists(String email, Long excludeUserId) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getUserId, excludeUserId);
        }
        
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean isMinecraftUuidBound(String minecraftUuid, Long excludeUserId) {
        if (!StringUtils.hasText(minecraftUuid)) {
            return false;
        }
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getMinecraftUuid, minecraftUuid);
        
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getUserId, excludeUserId);
        }
        
        return this.count(wrapper) > 0;
    }

    @Override
    public UserProfileVO getUserProfile(Long userId, Long currentUserId) {
        // 获取用户基本信息
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 构建响应对象
        UserProfileVO profileVO = UserProfileVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .location(user.getLocation())
                .website(user.getWebsite())
                .gender(user.getGender())
                .createTime(user.getCreateTime())
                .build();

        // 获取统计信息
        profileVO.setFollowerCount(userFollowService.getFollowerCount(userId));
        profileVO.setFollowingCount(userFollowService.getFollowingCount(userId));
        // TODO: 从 forum_post 表获取帖子数
        profileVO.setPostCount(0L);

        // 检查当前用户是否已关注
        if (currentUserId != null && !currentUserId.equals(userId)) {
            profileVO.setIsFollowing(userFollowService.isFollowing(currentUserId, userId));
        } else {
            profileVO.setIsFollowing(false);
        }

        return profileVO;
    }

    @Override
    public boolean updateUserProfile(Long userId, UserProfileUpdateDTO profileUpdateDTO) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        
        // 只更新非空字段
        if (StringUtils.hasText(profileUpdateDTO.getNickname())) {
            user.setNickname(profileUpdateDTO.getNickname());
        }
        if (StringUtils.hasText(profileUpdateDTO.getAvatar())) {
            user.setAvatar(profileUpdateDTO.getAvatar());
        }
        if (StringUtils.hasText(profileUpdateDTO.getBio())) {
            user.setBio(profileUpdateDTO.getBio());
        }
        if (StringUtils.hasText(profileUpdateDTO.getLocation())) {
            user.setLocation(profileUpdateDTO.getLocation());
        }
        if (StringUtils.hasText(profileUpdateDTO.getWebsite())) {
            user.setWebsite(profileUpdateDTO.getWebsite());
        }
        if (profileUpdateDTO.getGender() != null) {
            user.setGender(profileUpdateDTO.getGender());
        }

        return this.updateById(user);
    }

    @Override
    public boolean bindMinecraftAccount(Long userId, String minecraftUuid, String minecraftUsername) {
        // 检查用户是否存在
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查Minecraft UUID是否已被绑定
        if (isMinecraftUuidBound(minecraftUuid, userId)) {
            throw new RuntimeException("该Minecraft账号已被绑定");
        }
        
        // 更新用户信息
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getUserId, userId)
               .set(SysUser::getMinecraftUuid, minecraftUuid)
               .set(SysUser::getMinecraftUsername, minecraftUsername)
               .set(SysUser::getUpdateTime, LocalDateTime.now());
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("绑定Minecraft账号成功: userId={}, minecraftUuid={}, username={}", 
                    userId, minecraftUuid, minecraftUsername);
        }
        
        return success;
    }

    @Override
    public boolean unbindMinecraftAccount(Long userId) {
        // 检查用户是否存在
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查是否已绑定
        if (!StringUtils.hasText(user.getMinecraftUuid())) {
            throw new RuntimeException("用户未绑定Minecraft账号");
        }
        
        // 解绑Minecraft账号
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getUserId, userId)
               .set(SysUser::getMinecraftUuid, null)
               .set(SysUser::getMinecraftUsername, null)
               .set(SysUser::getUpdateTime, LocalDateTime.now());
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("解绑Minecraft账号成功: userId={}, previousUuid={}", 
                    userId, user.getMinecraftUuid());
        }
        
        return success;
    }



    @Override
    public MinecraftBindingInfo getMinecraftBindingInfo(Long userId) {
        // 获取用户信息
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        MinecraftBindingInfo info = new MinecraftBindingInfo();
        
        // 检查是否已绑定
        if (StringUtils.hasText(user.getMinecraftUuid())) {
            info.setBound(true);
            info.setMinecraftUuid(user.getMinecraftUuid());
            info.setMinecraftUsername(user.getMinecraftUsername());
            info.setStatus("已绑定");
            
            // 将java.util.Date转换为java.time.LocalDateTime
            if (user.getUpdateTime() != null) {
                info.setBindTime(user.getUpdateTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
            }
            
            // 生成Minecraft头像URL
            if (StringUtils.hasText(user.getMinecraftUuid())) {
                String uuid = user.getMinecraftUuid().replace("-", "");
                info.setAvatarUrl("https://crafatar.com/avatars/" + uuid + "?size=64&overlay");
            }
        } else {
            info.setBound(false);
            info.setStatus("未绑定");
        }
        
        return info;
    }
}
