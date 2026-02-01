package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.model.entity.PostHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子历史Mapper接口
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Mapper
public interface PostHistoryMapper extends BaseMapper<PostHistory> {
    
    /**
     * 查询帖子的编辑历史
     *
     * @param postId 帖子ID
     * @param limit 限制数量
     * @return 历史记录列表
     */
    @Select("SELECT * FROM forum_post_history WHERE post_id = #{postId} ORDER BY create_time DESC LIMIT #{limit}")
    List<PostHistory> selectByPostId(@Param("postId") Long postId, @Param("limit") int limit);
}
