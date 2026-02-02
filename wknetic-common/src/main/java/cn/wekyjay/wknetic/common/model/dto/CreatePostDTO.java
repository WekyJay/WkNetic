package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 创建帖子DTO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class CreatePostDTO {
    
    /**
     * 帖子标题
     */
    @NotBlank(message = "标题不能为空")
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
    @NotBlank(message = "内容不能为空")
    private String content;
    
    /**
     * 所属话题ID（发布时必填，草稿可以为空）
     */
    private Long topicId;
    
    /**
     * 标签列表（标签名称）
     */
    @Size(max = 10, message = "最多添加10个标签")
    private List<String> tags;
    
    /**
     * 是否直接发布（false=保存草稿，true=提交审核）
     */
    private Boolean publish = true;
}
