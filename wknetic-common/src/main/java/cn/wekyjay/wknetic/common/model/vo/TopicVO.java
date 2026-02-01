package cn.wekyjay.wknetic.common.model.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 话题VO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class TopicVO {
    
    /**
     * 话题ID
     */
    private Long topicId;
    
    /**
     * 话题名称
     */
    private String topicName;
    
    /**
     * 话题描述
     */
    private String topicDesc;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 颜色
     */
    private String color;
    
    /**
     * 帖子数量
     */
    private Integer postCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
