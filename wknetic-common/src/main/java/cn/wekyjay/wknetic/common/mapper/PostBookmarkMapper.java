package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.model.entity.PostBookmark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子收藏Mapper接口
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Mapper
public interface PostBookmarkMapper extends BaseMapper<PostBookmark> {
    
    /**
     * 查询用户收藏的帖子ID列表
     *
     * @param userId 用户ID
     * @param postIds 帖子ID列表
     * @return 收藏的帖子ID列表
     */
    @Select("<script>" +
            "SELECT post_id FROM forum_post_bookmark " +
            "WHERE user_id = #{userId} AND post_id IN " +
            "<foreach item='item' collection='postIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<Long> selectBookmarkedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
