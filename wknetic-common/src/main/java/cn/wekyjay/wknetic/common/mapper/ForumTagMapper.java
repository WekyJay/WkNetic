package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.model.entity.ForumTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 标签Mapper接口
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Mapper
public interface ForumTagMapper extends BaseMapper<ForumTag> {
    
    /**
     * 根据标签名称列表批量查询或创建标签
     *
     * @param tagNames 标签名称列表
     * @return 标签列表
     */
    List<ForumTag> selectOrCreateByNames(@Param("tagNames") List<String> tagNames);
    
    /**
     * 增加使用次数
     *
     * @param tagId 标签ID
     * @return 影响行数
     */
    @Update("UPDATE forum_tag SET use_count = use_count + 1 WHERE tag_id = #{tagId}")
    int incrementUseCount(@Param("tagId") Long tagId);
    
    /**
     * 减少使用次数
     *
     * @param tagId 标签ID
     * @return 影响行数
     */
    @Update("UPDATE forum_tag SET use_count = GREATEST(use_count - 1, 0) WHERE tag_id = #{tagId}")
    int decrementUseCount(@Param("tagId") Long tagId);
    
    /**
     * 获取热门标签
     *
     * @param limit 限制数量
     * @return 标签列表
     */
    @Select("SELECT * FROM forum_tag ORDER BY use_count DESC LIMIT #{limit}")
    List<ForumTag> selectHotTags(@Param("limit") int limit);
}
