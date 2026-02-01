package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.model.entity.CommentLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞Mapper接口
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
}
