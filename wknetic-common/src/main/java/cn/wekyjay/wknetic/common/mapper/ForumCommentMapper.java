package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.model.entity.ForumComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论Mapper接口
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Mapper
public interface ForumCommentMapper extends BaseMapper<ForumComment> {
    
    /**
     * 查询帖子的所有评论（包括嵌套回复）
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    @Select("SELECT * FROM forum_comment WHERE post_id = #{postId} AND status = 1 ORDER BY create_time ASC")
    List<ForumComment> selectByPostId(@Param("postId") Long postId);
    
    /**
     * 查询用户的所有评论
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 评论列表
     */
    @Select("SELECT * FROM forum_comment WHERE user_id = #{userId} AND status = 1 ORDER BY create_time DESC LIMIT #{limit}")
    List<ForumComment> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
