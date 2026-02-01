package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.common.domain.UserFollow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户关注 Service 接口
 */
public interface IUserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     *
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否成功
     */
    boolean followUser(Long followerId, Long followingId);

    /**
     * 取消关注用户
     *
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否成功
     */
    boolean unfollowUser(Long followerId, Long followingId);

    /**
     * 检查是否已关注
     *
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取粉丝数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    long getFollowerCount(Long userId);

    /**
     * 获取关注数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    long getFollowingCount(Long userId);
}
