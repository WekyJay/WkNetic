package cn.wekyjay.wknetic.admin.forum.service;

import cn.wekyjay.wknetic.admin.forum.document.PostDocument;
import cn.wekyjay.wknetic.community.event.EventPublisher;
import cn.wekyjay.wknetic.community.event.post.*;
import cn.wekyjay.wknetic.common.mapper.*;
import cn.wekyjay.wknetic.common.model.dto.CreatePostDTO;
import cn.wekyjay.wknetic.common.model.dto.SearchPostDTO;
import cn.wekyjay.wknetic.common.model.dto.UpdatePostDTO;
import cn.wekyjay.wknetic.common.model.entity.*;
import cn.wekyjay.wknetic.common.model.vo.PostDetailVO;
import cn.wekyjay.wknetic.common.model.vo.PostSearchVO;
import cn.wekyjay.wknetic.common.model.vo.PostVO;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子服务
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService extends ServiceImpl<ForumPostMapper, ForumPost> {
    
    private final ForumPostMapper postMapper;
    private final ForumTopicMapper topicMapper;
    private final ForumTagMapper tagMapper;
    private final PostTagMapper postTagMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostBookmarkMapper postBookmarkMapper;
    private final PostHistoryMapper postHistoryMapper;
    private final EventPublisher eventPublisher;
    private final ElasticsearchService elasticsearchService;
    
    /**
     * 创建帖子
     *
     * @param dto 创建帖子DTO
     * @return 帖子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(CreatePostDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 创建帖子实体
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setTopicId(dto.getTopicId());
        post.setTitle(dto.getTitle());
        post.setExcerpt(dto.getExcerpt());
        post.setContent(dto.getContent());
        
        // 设置状态：直接发布则进入审核，否则保存为草稿
        if (Boolean.TRUE.equals(dto.getPublish())) {
            post.setStatus(ForumPost.Status.UNDER_REVIEW.getCode());
        } else {
            post.setStatus(ForumPost.Status.DRAFT.getCode());
        }
        
        // 初始化计数器
        post.setIsPinned(false);
        post.setIsHot(false);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);
        post.setBookmarkCount(0);
        
        // 保存帖子
        postMapper.insert(post);
        
        // 处理标签
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            handlePostTags(post.getPostId(), dto.getTags());
        }
        
        // 更新话题帖子数
        topicMapper.incrementPostCount(dto.getTopicId());
        
        // 发布帖子创建事件
        if (Boolean.TRUE.equals(dto.getPublish())) {
            eventPublisher.publishEvent(new PostCreatedEvent(
                    this, post.getPostId(), userId, post.getTitle(), post.getTopicId()
            ));
        }
        
        log.info("用户{}创建帖子，ID: {}, 状态: {}", userId, post.getPostId(), post.getStatus());
        return post.getPostId();
    }
    
    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param dto 更新DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long postId, UpdatePostDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 查询原帖子
        ForumPost oldPost = postMapper.selectById(postId);
        if (oldPost == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        // 权限检查：只有作者或管理员可以编辑
        if (!oldPost.getUserId().equals(userId) && !SecurityUtils.isAdmin()) {
            throw new RuntimeException("无权编辑此帖子");
        }
        
        // 保存编辑历史
        savePostHistory(oldPost, userId, dto.getChangeSummary());
        
        // 更新帖子
        ForumPost post = new ForumPost();
        post.setPostId(postId);
        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
        }
        if (dto.getExcerpt() != null) {
            post.setExcerpt(dto.getExcerpt());
        }
        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }
        if (dto.getTopicId() != null && !dto.getTopicId().equals(oldPost.getTopicId())) {
            // 更新话题计数
            topicMapper.decrementPostCount(oldPost.getTopicId());
            topicMapper.incrementPostCount(dto.getTopicId());
            post.setTopicId(dto.getTopicId());
        }
        
        postMapper.updateById(post);
        
        // 更新标签
        if (dto.getTags() != null) {
            // 删除旧标签关联
            postTagMapper.delete(new LambdaQueryWrapper<PostTag>()
                    .eq(PostTag::getPostId, postId));
            // 添加新标签
            if (!dto.getTags().isEmpty()) {
                handlePostTags(postId, dto.getTags());
            }
        }
        
        // 发布更新事件
        eventPublisher.publishEvent(new PostUpdatedEvent(
                this, postId, userId, dto.getChangeSummary()
        ));
        
        log.info("用户{}更新帖子{}", userId, postId);
    }
    
    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        // 权限检查
        if (!post.getUserId().equals(userId) && !SecurityUtils.isAdmin()) {
            throw new RuntimeException("无权删除此帖子");
        }
        
        // 软删除：更新状态
        ForumPost update = new ForumPost();
        update.setPostId(postId);
        update.setStatus(ForumPost.Status.DELETED.getCode());
        postMapper.updateById(update);
        
        // 更新话题计数
        topicMapper.decrementPostCount(post.getTopicId());
        
        // 发布删除事件
        eventPublisher.publishEvent(new PostDeletedEvent(
                this, postId, post.getUserId(), userId, "用户删除"
        ));
        
        log.info("用户{}删除帖子{}", userId, postId);
    }
    
    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情VO
     */
    public PostDetailVO getPostDetail(Long postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        // 增加浏览数
        postMapper.incrementViewCount(postId);
        
        // TODO: 转换为VO（需要关联查询作者、话题、标签等信息）
        PostDetailVO vo = new PostDetailVO();
        vo.setPostId(post.getPostId());
        vo.setTitle(post.getTitle());
        vo.setExcerpt(post.getExcerpt());
        vo.setContent(post.getContent());
        vo.setContentHtml(post.getContentHtml());
        vo.setStatus(post.getStatus());
        vo.setIsPinned(post.getIsPinned());
        vo.setIsHot(post.getIsHot());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setViewCount(post.getViewCount());
        vo.setBookmarkCount(post.getBookmarkCount());
        vo.setCreateTime(post.getCreateTime());
        vo.setUpdateTime(post.getUpdateTime());
        
        // 检查当前用户是否点赞/收藏
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            vo.setIsLiked(checkUserLiked(postId, userId));
            vo.setIsBookmarked(checkUserBookmarked(postId, userId));
        }
        
        return vo;
    }
    
    /**
     * 分页查询帖子列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param topicId 话题ID（可选）
     * @param status 状态（可选）
     * @return 帖子列表
     */
    public IPage<PostVO> listPosts(int page, int size, Long topicId, Integer status) {
        Page<ForumPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        
        if (topicId != null) {
            wrapper.eq(ForumPost::getTopicId, topicId);
        }
        if (status != null) {
            wrapper.eq(ForumPost::getStatus, status);
        } else {
            // 默认只显示已发布的
            wrapper.eq(ForumPost::getStatus, ForumPost.Status.PUBLISHED.getCode());
        }
        
        // 排序：置顶优先，然后按最后评论时间
        wrapper.orderByDesc(ForumPost::getIsPinned)
                .orderByDesc(ForumPost::getLastCommentTime)
                .orderByDesc(ForumPost::getCreateTime);
        
        IPage<ForumPost> postPage = postMapper.selectPage(pageParam, wrapper);
        
        // TODO: 转换为VO（需要关联查询）
        return postPage.convert(post -> {
            PostVO vo = new PostVO();
            vo.setPostId(post.getPostId());
            vo.setTitle(post.getTitle());
            vo.setExcerpt(post.getExcerpt());
            // ... 其他字段
            return vo;
        });
    }
    
    /**
     * 点赞/取消点赞
     *
     * @param postId 帖子ID
     * @return true=点赞，false=取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 检查是否已点赞
        PostLike existingLike = postLikeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, userId)
        );
        
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        boolean liked;
        if (existingLike != null) {
            // 取消点赞
            postLikeMapper.deleteById(existingLike.getId());
            // 减少点赞数
            ForumPost update = new ForumPost();
            update.setPostId(postId);
            update.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(update);
            liked = false;
        } else {
            // 点赞
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            // 增加点赞数
            ForumPost update = new ForumPost();
            update.setPostId(postId);
            update.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(update);
            liked = true;
        }
        
        // 发布点赞事件
        eventPublisher.publishEvent(new PostLikedEvent(
                this, postId, post.getUserId(), userId, !liked
        ));
        
        return liked;
    }
    
    /**
     * 处理帖子标签
     */
    private void handlePostTags(Long postId, List<String> tagNames) {
        for (String tagName : tagNames) {
            // 查找或创建标签
            ForumTag tag = tagMapper.selectOne(
                    new LambdaQueryWrapper<ForumTag>()
                            .eq(ForumTag::getTagName, tagName)
            );
            
            if (tag == null) {
                tag = new ForumTag();
                tag.setTagName(tagName);
                tag.setUseCount(0);
                tagMapper.insert(tag);
            }
            
            // 创建关联
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tag.getTagId());
            postTagMapper.insert(postTag);
            
            // 增加使用次数
            tagMapper.incrementUseCount(tag.getTagId());
        }
    }
    
    /**
     * 保存编辑历史
     */
    private void savePostHistory(ForumPost post, Long editorId, String changeSummary) {
        PostHistory history = new PostHistory();
        history.setPostId(post.getPostId());
        history.setEditorId(editorId);
        history.setTitle(post.getTitle());
        history.setContent(post.getContent());
        history.setContentHtml(post.getContentHtml());
        history.setChangeSummary(changeSummary);
        postHistoryMapper.insert(history);
    }
    
    /**
     * 检查用户是否点赞
     */
    private boolean checkUserLiked(Long postId, Long userId) {
        return postLikeMapper.selectCount(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, userId)
        ) > 0;
    }
    
    /**
     * 检查用户是否收藏
     */
    private boolean checkUserBookmarked(Long postId, Long userId) {
        return postBookmarkMapper.selectCount(
                new LambdaQueryWrapper<PostBookmark>()
                        .eq(PostBookmark::getPostId, postId)
                        .eq(PostBookmark::getUserId, userId)
        ) > 0;
    }
    
    /**
     * 搜索帖子（基于Elasticsearch）
     */
    public IPage<PostSearchVO> searchPosts(SearchPostDTO dto) {
        // 调用Elasticsearch服务进行搜索
        org.springframework.data.domain.Page<PostDocument> esPage = elasticsearchService.searchPosts(
            dto.getKeyword(),
            dto.getTopicId(),
            dto.getTags(),
            dto.getStatus(),
            dto.getStartTime(),
            dto.getEndTime(),
            dto.getSortBy(),
            dto.getSortOrder(),
            dto.getPage(),
            dto.getSize()
        );
        
        // 转换为 PostSearchVO
        List<PostSearchVO> voList = esPage.getContent().stream()
            .map(doc -> PostSearchVO.builder()
                .postId(doc.getPostId())
                .title(doc.getTitle())
                .excerpt(doc.getExcerpt())
                .userId(doc.getUserId())
                .username(doc.getUsername())
                .topicId(doc.getTopicId())
                .topicName(doc.getTopicName())
                .tags(doc.getTags())
                .status(doc.getStatus())
                .isPinned(doc.getIsPinned())
                .isHot(doc.getIsHot())
                .likeCount(doc.getLikeCount())
                .commentCount(doc.getCommentCount())
                .viewCount(doc.getViewCount())
                .bookmarkCount(doc.getBookmarkCount())
                .createTime(doc.getCreateTime())
                .updateTime(doc.getUpdateTime())
                .lastCommentTime(doc.getLastCommentTime())
                .build())
            .collect(Collectors.toList());
        
        // 转换为 MyBatis-Plus IPage
        Page<PostSearchVO> page = new Page<>(dto.getPage() + 1, dto.getSize());
        page.setRecords(voList);
        page.setTotal(esPage.getTotalElements());
        
        return page;
    }
}

