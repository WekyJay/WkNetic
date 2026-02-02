package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.document.PostDocument;
import cn.wekyjay.wknetic.admin.forum.service.ElasticsearchService;
import cn.wekyjay.wknetic.common.mapper.*;
import cn.wekyjay.wknetic.common.model.entity.*;
import cn.wekyjay.wknetic.common.domain.SysUser;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 管理控制器
 * 用于索引管理和数据同步
 * 
 * @author WkNetic
 * @since 2026-02-02
 */
@Slf4j
@Tag(name = "Elasticsearch管理", description = "索引管理相关接口")
@RestController
@RequestMapping("/api/v1/admin/search")
@RequiredArgsConstructor
public class SearchAdminController {
    
    private final ElasticsearchService elasticsearchService;
    private final ForumPostMapper postMapper;
    private final ForumTopicMapper topicMapper;
    private final SysUserMapper userMapper;
    private final PostTagMapper postTagMapper;
    private final ForumTagMapper tagMapper;
    
    /**
     * 全量重建索引
     * 从数据库读取所有已发布的帖子并重新索引
     */
    @Operation(summary = "全量重建索引", description = "从数据库读取所有已发布帖子并重新建立搜索索引。此操作可能耗时较长。")
    @PostMapping("/rebuild-index")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> rebuildIndex() {
        log.info("开始全量重建搜索索引");
        
        try {
            // 查询所有已发布的帖子
            List<ForumPost> posts = postMapper.selectList(
                new LambdaQueryWrapper<ForumPost>()
                    .eq(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode())
            );
            
            log.info("找到 {} 个已发布帖子", posts.size());
            
            // 批量转换并索引
            List<PostDocument> documents = new ArrayList<>();
            
            for (ForumPost post : posts) {
                try {
                    // 获取作者信息
                    SysUser user = userMapper.selectById(post.getUserId());
                    String username = user != null ? user.getUsername() : "Unknown";
                    
                    // 获取话题信息
                    ForumTopic topic = topicMapper.selectById(post.getTopicId());
                    String topicName = topic != null ? topic.getTopicName() : "Unknown";
                    
                    // 获取标签列表
                    List<PostTag> postTags = postTagMapper.selectList(
                        new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, post.getPostId())
                    );
                    List<String> tags = postTags.stream()
                        .map(pt -> {
                            ForumTag tag = tagMapper.selectById(pt.getTagId());
                            return tag != null ? tag.getTagName() : null;
                        })
                        .filter(t -> t != null)
                        .collect(Collectors.toList());
                    
                    // 转换为文档
                    PostDocument document = elasticsearchService.convertToDocument(
                        post, username, topicName, tags
                    );
                    documents.add(document);
                    
                } catch (Exception e) {
                    log.error("处理帖子失败: postId={}", post.getPostId(), e);
                }
            }
            
            // 批量索引
            elasticsearchService.indexPosts(documents);
            
            String message = String.format("索引重建完成，共索引 %d 个帖子", documents.size());
            log.info(message);
            
            return Result.success(message);
            
        } catch (Exception e) {
            log.error("索引重建失败", e);
            return Result.error("索引重建失败: " + e.getMessage());
        }
    }
    
    /**
     * 索引单个帖子
     */
    @Operation(summary = "索引单个帖子", description = "将指定帖子添加到搜索索引")
    @PostMapping("/index-post/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> indexPost(@PathVariable Long postId) {
        log.info("索引帖子: postId={}", postId);
        
        try {
            ForumPost post = postMapper.selectById(postId);
            if (post == null) {
                return Result.error("帖子不存在");
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
            
            // 转换并索引
            PostDocument document = elasticsearchService.convertToDocument(
                post, username, topicName, tags
            );
            elasticsearchService.indexPost(document);
            
            return Result.success("索引成功");
            
        } catch (Exception e) {
            log.error("索引帖子失败: postId={}", postId, e);
            return Result.error("索引失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除帖子索引
     */
    @Operation(summary = "删除帖子索引", description = "从搜索索引中删除指定帖子")
    @DeleteMapping("/delete-post/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deletePost(@PathVariable Long postId) {
        log.info("删除帖子索引: postId={}", postId);
        
        try {
            elasticsearchService.deletePost(postId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除帖子索引失败: postId={}", postId, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
