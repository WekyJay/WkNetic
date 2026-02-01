package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子编辑历史实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_post_history")
public class PostHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 历史记录ID
     */
    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 编辑者ID
     */
    private Long editorId;
    
    /**
     * 历史标题
     */
    private String title;
    
    /**
     * 历史Markdown内容
     */
    private String content;
    
    /**
     * 历史HTML内容
     */
    private String contentHtml;
    
    /**
     * 修改摘要说明
     */
    private String changeSummary;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
