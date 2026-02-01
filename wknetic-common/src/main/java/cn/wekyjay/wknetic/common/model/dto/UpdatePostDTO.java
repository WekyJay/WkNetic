package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 更新帖子DTO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class UpdatePostDTO {
    
    /**
     * 帖子标题
     */
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;
    
    /**
     * 帖子简介
     */
    @Size(max = 500, message = "简介长度不能超过500个字符")
    private String excerpt;
    
    /**
     * Markdown内容
     */
    private String content;
    
    /**
     * 所属话题ID
     */
    private Long topicId;
    
    /**
     * 标签列表
     */
    @Size(max = 10, message = "最多添加10个标签")
    private List<String> tags;
    
    /**
     * 修改摘要说明
     */
    @Size(max = 500, message = "修改摘要长度不能超过500个字符")
    private String changeSummary;
}
