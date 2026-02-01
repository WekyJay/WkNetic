package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.admin.system.service.IUserFollowService;
import cn.wekyjay.wknetic.common.domain.UserFollow;
import cn.wekyjay.wknetic.common.mapper.UserFollowMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * 用户关注 Service 实现
 */
@Slf4j
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements IUserFollowService {

    private final UserFollowMapper userFollowMapper;

    public UserFollowServiceImpl(UserFollowMapper userFollowMapper) {
        this.userFollowMapper = userFollowMapper;
    }

    @Override
    public boolean followUser(Long followerId, Long followingId) {
        // 防止自己关注自己
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }

        // 检查是否已关注
        if (isFollowing(followerId, followingId)) {
            return true;
        }

        // 创建关注关系
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowingId(followingId);
        userFollow.setStatus(1);
        userFollow.setCreateTime(new Date());
        userFollow.setUpdateTime(new Date());

        return this.save(userFollow);
    }

    @Override
    public boolean unfollowUser(Long followerId, Long followingId) {
        // 检查是否已关注
        if (!isFollowing(followerId, followingId)) {
            return true;
        }

        // 更新关注状态为取消
        LambdaUpdateWrapper<UserFollow> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId)
                .set(UserFollow::setStatus, 0);

        return this.update(updateWrapper);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowMapper.isFollowing(followerId, followingId);
    }

    @Override
    public long getFollowerCount(Long userId) {
        return userFollowMapper.getFollowerCount(userId);
    }

    @Override
    public long getFollowingCount(Long userId) {
        return userFollowMapper.getFollowingCount(userId);
    }
}
