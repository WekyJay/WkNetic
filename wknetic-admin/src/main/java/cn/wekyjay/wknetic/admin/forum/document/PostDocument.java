package cn.wekyjay.wknetic.admin.forum.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子搜索文档
 * 
 * @author WkNetic
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "post_index")
@Setting(settingPath = "elasticsearch/post-index-settings.json")
public class PostDocument {
    
    /**
     * 帖子ID
     */
    @Id
    private Long postId;
    
    /**
     * 帖子标题（使用IK分词器，权重最高）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    /**
     * 帖子摘要（使用IK分词器）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String excerpt;
    
    /**
     * 帖子内容（使用IK分词器，权重较低）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    
    /**
     * 作者ID（精确匹配）
     */
    @Field(type = FieldType.Long)
    private Long userId;
    
    /**
     * 作者用户名（用于显示）
     */
    @Field(type = FieldType.Keyword)
    private String username;
    
    /**
     * 所属话题ID（用于过滤）
     */
    @Field(type = FieldType.Long)
    private Long topicId;
    
    /**
     * 话题名称（用于显示）
     */
    @Field(type = FieldType.Keyword)
    private String topicName;
    
    /**
     * 标签列表（用于过滤和聚合）
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    
    /**
     * 状态：0-草稿 1-已发布 2-审核中 3-已拒绝 4-已删除
     */
    @Field(type = FieldType.Integer)
    private Integer status;
    
    /**
     * 是否置顶
     */
    @Field(type = FieldType.Boolean)
    private Boolean isPinned;
    
    /**
     * 是否热门
     */
    @Field(type = FieldType.Boolean)
    private Boolean isHot;
    
    /**
     * 点赞数（用于排序）
     */
    @Field(type = FieldType.Integer)
    private Integer likeCount;
    
    /**
     * 评论数（用于排序）
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;
    
    /**
     * 浏览数（用于排序）
     */
    @Field(type = FieldType.Integer)
    private Integer viewCount;
    
    /**
     * 收藏数（用于排序）
     */
    @Field(type = FieldType.Integer)
    private Integer bookmarkCount;
    
    /**
     * 创建时间（用于过滤和排序）
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
    
    /**
     * 更新时间（用于排序）
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
    
    /**
     * 最后评论时间（用于排序）
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime lastCommentTime;
}
