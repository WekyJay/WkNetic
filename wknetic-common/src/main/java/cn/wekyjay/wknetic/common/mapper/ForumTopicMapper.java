package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.model.entity.ForumTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 话题Mapper接口
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Mapper
public interface ForumTopicMapper extends BaseMapper<ForumTopic> {
    
    /**
     * 增加帖子数
     *
     * @param topicId 话题ID
     * @return 影响行数
     */
    @Update("UPDATE forum_topic SET post_count = post_count + 1 WHERE topic_id = #{topicId}")
    int incrementPostCount(@Param("topicId") Long topicId);
    
    /**
     * 减少帖子数
     *
     * @param topicId 话题ID
     * @return 影响行数
     */
    @Update("UPDATE forum_topic SET post_count = GREATEST(post_count - 1, 0) WHERE topic_id = #{topicId}")
    int decrementPostCount(@Param("topicId") Long topicId);
}
