package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.domain.UserFollow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户关注 Mapper 接口
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 检查是否已关注
     *
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    @Select("SELECT COUNT(*) > 0 FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId} AND status = 1")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 获取粉丝数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId} AND status = 1")
    long getFollowerCount(@Param("userId") Long userId);

    /**
     * 获取关注数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId} AND status = 1")
    long getFollowingCount(@Param("userId") Long userId);
}
