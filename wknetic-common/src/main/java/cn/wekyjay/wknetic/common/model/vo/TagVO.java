package cn.wekyjay.wknetic.common.model.vo;

import lombok.Data;

/**
 * 标签VO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class TagVO {
    
    /**
     * 标签ID
     */
    private Long tagId;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * 使用次数
     */
    private Integer useCount;
}
