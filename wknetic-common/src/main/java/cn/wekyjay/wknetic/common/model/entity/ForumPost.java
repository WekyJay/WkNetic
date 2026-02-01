package cn.wekyjay.wknetic.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@TableName("forum_post")
public class ForumPost implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 帖子ID
     */
    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;
    
    /**
     * 发帖用户ID
     */
    private Long userId;
    
    /**
     * 所属话题ID
     */
    private Long topicId;
    
    /**
     * 帖子标题
     */
    private String title;
    
    /**
     * 帖子简介/摘要
     */
    private String excerpt;
    
    /**
     * Markdown格式内容
     */
    private String content;
    
    /**
     * 缓存的HTML内容
     */
    private String contentHtml;
    
    /**
     * 状态：0-草稿 1-已发布 2-审核中 3-已拒绝 4-已删除
     */
    private Integer status;
    
    /**
     * 是否置顶
     */
    private Boolean isPinned;
    
    /**
     * 是否热门
     */
    private Boolean isHot;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 收藏数
     */
    private Integer bookmarkCount;
    
    /**
     * 审核人ID
     */
    private Long auditUserId;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 审核备注
     */
    private String auditRemark;
    
    /**
     * 最后评论时间
     */
    private LocalDateTime lastCommentTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 帖子状态枚举
     */
    public enum Status {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "已发布"),
        UNDER_REVIEW(2, "审核中"),
        REJECTED(3, "已拒绝"),
        DELETED(4, "已删除");
        
        private final int code;
        private final String desc;
        
        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
    }
}
