package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建/更新收藏分类DTO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class BookmarkCategoryDTO {
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String categoryName;
    
    /**
     * 分类描述
     */
    @Size(max = 200, message = "分类描述长度不能超过200个字符")
    private String description;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
}
