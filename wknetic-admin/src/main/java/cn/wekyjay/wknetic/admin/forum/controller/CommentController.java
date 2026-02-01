package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.CommentService;
import cn.wekyjay.wknetic.common.model.dto.CreateCommentDTO;
import cn.wekyjay.wknetic.common.model.vo.CommentVO;
import cn.wekyjay.wknetic.common.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论Controller
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Tag(name = "评论管理", description = "评论相关接口")
@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * 创建评论
     */
    @Operation(summary = "创建评论")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createComment(@Valid @RequestBody CreateCommentDTO dto) {
        Long commentId = commentService.createComment(dto);
        return Result.success(commentId);
    }
    
    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return Result.success();
    }
    
    /**
     * 获取帖子评论列表（树形结构）
     */
    @Operation(summary = "获取帖子评论列表")
    @GetMapping("/post/{postId}")
    public Result<List<CommentVO>> getPostComments(@PathVariable Long postId) {
        List<CommentVO> comments = commentService.getPostComments(postId);
        return Result.success(comments);
    }
    
    /**
     * 点赞/取消点赞评论
     */
    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/{commentId}/like")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> toggleLike(@PathVariable Long commentId) {
        boolean liked = commentService.toggleLike(commentId);
        return Result.success(liked);
    }
}
