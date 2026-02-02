package cn.wekyjay.wknetic.admin.forum.service;

import cn.wekyjay.wknetic.community.event.EventPublisher;
import cn.wekyjay.wknetic.community.event.comment.CommentCreatedEvent;
import cn.wekyjay.wknetic.community.event.comment.CommentDeletedEvent;
import cn.wekyjay.wknetic.community.event.comment.CommentLikedEvent;
import cn.wekyjay.wknetic.common.mapper.*;
import cn.wekyjay.wknetic.common.model.dto.CreateCommentDTO;
import cn.wekyjay.wknetic.common.model.entity.CommentLike;
import cn.wekyjay.wknetic.common.model.entity.ForumComment;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import cn.wekyjay.wknetic.common.model.vo.CommentVO;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService extends ServiceImpl<ForumCommentMapper, ForumComment> {
    
    private final ForumCommentMapper commentMapper;
    private final ForumPostMapper postMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final EventPublisher eventPublisher;
    
    /**
     * 创建评论
     *
     * @param dto 创建评论DTO
     * @return 评论ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CreateCommentDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 检查帖子是否存在
        ForumPost post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        // 创建评论
        ForumComment comment = new ForumComment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1); // 1-正常
        
        commentMapper.insert(comment);
        
        // 更新帖子评论数和最后评论时间
        postMapper.incrementCommentCount(dto.getPostId());
        postMapper.updateLastCommentTime(dto.getPostId());
        
        // 发布评论创建事件
        eventPublisher.publishEvent(new CommentCreatedEvent(
                this,
                comment.getCommentId(),
                dto.getPostId(),
                post.getUserId(),
                userId,
                dto.getParentId(),
                dto.getReplyToUserId()
        ));
        
        log.info("用户{}在帖子{}创建评论{}", userId, dto.getPostId(), comment.getCommentId());
        return comment.getCommentId();
    }
    
    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        ForumComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        // 权限检查
        if (!comment.getUserId().equals(userId) && !SecurityUtils.isAdmin()) {
            throw new RuntimeException("无权删除此评论");
        }
        
        // 软删除
        ForumComment update = new ForumComment();
        update.setCommentId(commentId);
        update.setStatus(2); // 2-已删除
        commentMapper.updateById(update);
        
        // 更新帖子评论数
        postMapper.decrementCommentCount(comment.getPostId());
        
        // 发布删除事件
        eventPublisher.publishEvent(new CommentDeletedEvent(
                this,
                commentId,
                comment.getPostId(),
                comment.getUserId(),
                userId,
                "用户删除"
        ));
        
        log.info("用户{}删除评论{}", userId, commentId);
    }
    
    /**
     * 获取帖子的评论列表（树形结构）
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    public List<CommentVO> getPostComments(Long postId) {
        // 查询所有评论
        List<ForumComment> comments = commentMapper.selectByPostId(postId);
        
        // 构建树形结构
        return buildCommentTree(comments);
    }
    
    /**
     * 获取帖子的评论列表（分页）
     *
     * @param postId 帖子ID
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 分页评论列表
     */
    public Map<String, Object> listCommentsByPostId(Long postId, Integer page, Integer size) {
        // 计算offset
        int offset = (page - 1) * size;
        
        // 查询评论总数
        long total = commentMapper.selectCount(
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .eq(ForumComment::getParentId, null)  // 只计算顶级评论
        );
        
        // 查询分页评论
        List<ForumComment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .eq(ForumComment::getParentId, null)  // 只获取顶级评论
                        .orderByDesc(ForumComment::getCreateTime)
                        .last("LIMIT " + offset + ", " + size)
        );
        
        // 获取所有子评论
        List<Long> commentIds = comments.stream().map(ForumComment::getCommentId).collect(Collectors.toList());
        List<ForumComment> replies = commentMapper.selectList(
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .in(ForumComment::getParentId, commentIds)
        );
        
        // 构建评论树
        Map<Long, List<ForumComment>> childrenMap = new HashMap<>();
        for (ForumComment reply : replies) {
            childrenMap.computeIfAbsent(reply.getParentId(), k -> new ArrayList<>())
                    .add(reply);
        }
        
        List<CommentVO> records = comments.stream()
                .map(comment -> buildCommentVO(comment, childrenMap))
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        
        return result;
    }
    
    /**
     * 点赞/取消点赞评论
     *
     * @param commentId 评论ID
     * @return true=点赞，false=取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 检查是否已点赞
        CommentLike existingLike = commentLikeMapper.selectOne(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId)
        );
        
        ForumComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        boolean liked;
        if (existingLike != null) {
            // 取消点赞
            commentLikeMapper.deleteById(existingLike.getId());
            ForumComment update = new ForumComment();
            update.setCommentId(commentId);
            update.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentMapper.updateById(update);
            liked = false;
        } else {
            // 点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            ForumComment update = new ForumComment();
            update.setCommentId(commentId);
            update.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(update);
            liked = true;
        }
        
        // 发布点赞事件
        eventPublisher.publishEvent(new CommentLikedEvent(
                this, commentId, comment.getUserId(), userId, !liked
        ));
        
        return liked;
    }
    
    /**
     * 构建评论树
     */
    private List<CommentVO> buildCommentTree(List<ForumComment> comments) {
        // 按父ID分组
        Map<Long, List<ForumComment>> childrenMap = new HashMap<>();
        List<ForumComment> roots = new ArrayList<>();
        
        for (ForumComment comment : comments) {
            if (comment.getParentId() == null) {
                roots.add(comment);
            } else {
                childrenMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>())
                        .add(comment);
            }
        }
        
        // 递归构建树
        return roots.stream()
                .map(comment -> buildCommentVO(comment, childrenMap))
                .collect(Collectors.toList());
    }
    
    /**
     * 构建评论VO（递归）
     */
    private CommentVO buildCommentVO(ForumComment comment, Map<Long, List<ForumComment>> childrenMap) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getCommentId());
        vo.setContent(comment.getContent());
        vo.setContentHtml(comment.getContentHtml());
        vo.setLikeCount(comment.getLikeCount());
        vo.setParentId(comment.getParentId());
        vo.setStatus(comment.getStatus());
        vo.setCreateTime(comment.getCreateTime());
        vo.setUpdateTime(comment.getUpdateTime());
        
        // TODO: 加载作者信息
        
        // 递归加载子评论
        List<ForumComment> children = childrenMap.get(comment.getCommentId());
        if (children != null && !children.isEmpty()) {
            vo.setReplies(children.stream()
                    .map(child -> buildCommentVO(child, childrenMap))
                    .collect(Collectors.toList()));
        }
        
        return vo;
    }
}
