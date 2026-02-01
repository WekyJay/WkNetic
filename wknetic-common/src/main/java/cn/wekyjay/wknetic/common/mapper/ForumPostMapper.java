package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子Mapper接口
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {
    
    /**
     * 增加浏览数
     *
     * @param postId 帖子ID
     * @return 影响行数
     */
    @Update("UPDATE forum_post SET view_count = view_count + 1 WHERE post_id = #{postId}")
    int incrementViewCount(@Param("postId") Long postId);
    
    /**
     * 增加评论数
     *
     * @param postId 帖子ID
     * @return 影响行数
     */
    @Update("UPDATE forum_post SET comment_count = comment_count + 1 WHERE post_id = #{postId}")
    int incrementCommentCount(@Param("postId") Long postId);
    
    /**
     * 减少评论数
     *
     * @param postId 帖子ID
     * @return 影响行数
     */
    @Update("UPDATE forum_post SET comment_count = GREATEST(comment_count - 1, 0) WHERE post_id = #{postId}")
    int decrementCommentCount(@Param("postId") Long postId);
    
    /**
     * 更新最后评论时间
     *
     * @param postId 帖子ID
     * @return 影响行数
     */
    @Update("UPDATE forum_post SET last_comment_time = NOW() WHERE post_id = #{postId}")
    int updateLastCommentTime(@Param("postId") Long postId);
}
