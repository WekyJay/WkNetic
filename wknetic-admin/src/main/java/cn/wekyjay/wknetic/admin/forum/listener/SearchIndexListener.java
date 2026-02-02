package cn.wekyjay.wknetic.admin.forum.listener;

import cn.wekyjay.wknetic.admin.forum.document.PostDocument;
import cn.wekyjay.wknetic.admin.forum.service.ElasticsearchService;
import cn.wekyjay.wknetic.common.mapper.ForumPostMapper;
import cn.wekyjay.wknetic.common.mapper.ForumTagMapper;
import cn.wekyjay.wknetic.common.mapper.ForumTopicMapper;
import cn.wekyjay.wknetic.common.mapper.PostTagMapper;
import cn.wekyjay.wknetic.common.mapper.SysUserMapper;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import cn.wekyjay.wknetic.common.model.entity.ForumTag;
import cn.wekyjay.wknetic.common.model.entity.ForumTopic;
import cn.wekyjay.wknetic.common.model.entity.PostTag;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.community.event.post.*;
import cn.wekyjay.wknetic.community.event.comment.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索索引监听器
 * 监听内容变更事件并同步到Elasticsearch
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexListener {
    
    private final ElasticsearchService elasticsearchService;
    private final ForumPostMapper postMapper;
    private final ForumTopicMapper topicMapper;
    private final SysUserMapper userMapper;
    private final PostTagMapper postTagMapper;
    private final ForumTagMapper tagMapper;
    
    /**
     * 监听帖子创建事件
     * 创建搜索索引
     */
    @Async
    @EventListener
    public void onPostCreated(PostCreatedEvent event) {
        log.info("帖子创建，同步到搜索引擎: postId={}", event.getPostId());
        
        try {
            // 如果需要审核，等审核通过后再索引
            if (event.isNeedsReview()) {
                log.debug("帖子需要审核，等待审核通过后再索引: postId={}", event.getPostId());
                return;
            }
            
            // 获取帖子数据并索引
            indexPost(event.getPostId());
        } catch (Exception e) {
            log.error("索引帖子失败: postId={}", event.getPostId(), e);
        }
    }
    
    /**
     * 监听帖子更新事件
     * 更新搜索索引
     */
    @Async
    @EventListener
    public void onPostUpdated(PostUpdatedEvent event) {
        log.info("帖子更新，更新搜索索引: postId={}", event.getPostId());
        
        try {
            // 更新索引
            indexPost(event.getPostId());
        } catch (Exception e) {
            log.error("更新帖子索引失败: postId={}", event.getPostId(), e);
        }
    }
    
    /**
     * 监听帖子审核事件
     * 审核通过时创建索引，审核拒绝时删除索引
     */
    @Async
    @EventListener
    public void onPostAudited(PostAuditedEvent event) {
        log.info("帖子审核完成，更新搜索索引: postId={}, approved={}", 
                event.getPostId(), event.isApproved());
        
        try {
            if (event.isApproved()) {
                // 审核通过，索引帖子
                indexPost(event.getPostId());
            } else {
                // 审核拒绝，删除索引
                elasticsearchService.deletePost(event.getPostId());
            }
        } catch (Exception e) {
            log.error("处理帖子审核事件失败: postId={}", event.getPostId(), e);
        }
    }
    
    /**
     * 监听帖子删除事件
     * 删除搜索索引
     */
    @Async
    @EventListener
    public void onPostDeleted(PostDeletedEvent event) {
        log.info("帖子删除，删除搜索索引: postId={}", event.getPostId());
        
        try {
            elasticsearchService.deletePost(event.getPostId());
        } catch (Exception e) {
            log.error("删除帖子索引失败: postId={}", event.getPostId(), e);
        }
    }
    
    /**
     * 监听评论创建事件
     * 评论也可以被搜索，创建评论索引
     */
    @Async
    @EventListener
    public void onCommentCreated(CommentCreatedEvent event) {
        log.debug("评论创建，同步到搜索引擎: commentId={}, postId={}", 
                event.getCommentId(), event.getPostId());
        
        // 评论创建时，可以更新帖子的评论计数
        // 这里选择不重新索引整个帖子，只在统计数据发生变化时更新
    }
    
    /**
     * 索引单个帖子
     */
    private void indexPost(Long postId) {
        // 获取帖子信息
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            log.warn("帖子不存在: postId={}", postId);
            return;
        }
        
        // 只索引已发布的帖子
        if (post.getStatus() == null || !post.getStatus().equals(ForumPost.Status.PUBLISHED.getCode())) {
            log.debug("帖子未发布，跳过索引: postId={}, status={}", postId, post.getStatus());
            return;
        }
        
        // 获取作者信息
        SysUser user = userMapper.selectById(post.getUserId());
        String username = user != null ? user.getUsername() : "Unknown";
        
        // 获取话题信息
        ForumTopic topic = topicMapper.selectById(post.getTopicId());
        String topicName = topic != null ? topic.getTopicName() : "Unknown";
        
        // 获取标签列表
        List<PostTag> postTags = postTagMapper.selectList(
            new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId)
        );
        List<String> tags = postTags.stream()
            .map(pt -> {
                ForumTag tag = tagMapper.selectById(pt.getTagId());
                return tag != null ? tag.getTagName() : null;
            })
            .filter(t -> t != null)
            .collect(Collectors.toList());
        
        // 转换为文档并索引
        PostDocument document = elasticsearchService.convertToDocument(post, username, topicName, tags);
        elasticsearchService.indexPost(document);
    }
}
