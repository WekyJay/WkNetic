package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.CommentService;
import cn.wekyjay.wknetic.common.model.dto.CreateCommentDTO;
import cn.wekyjay.wknetic.common.model.vo.CommentVO;
import cn.wekyjay.wknetic.common.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * 创建评论 - 为帖子下新增评论或回复
     */
    @Operation(summary = "创建评论", description = "为帖子或子评论新增一个评论。支持回复功能。")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createComment(@Valid @RequestBody CreateCommentDTO dto) {
        Long commentId = commentService.createComment(dto);
        return Result.success(commentId);
    }
    
    /**
     * 删除评论 - 软删除评论（标记为已删除）
     */
    @Operation(summary = "删除评论", description = "删除指定的评论。只有评论作者或管理员可以删除。")
    @Parameters({
            @Parameter(name = "commentId", description = "评论会idId", required = true, example = "1")
    })
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return Result.success();
    }
    
    /**
     * 获取帖子评论列表 - 以树形结构返回
     */
    @Operation(summary = "获取帖子评论列表", description = "获取指定帖子的所有评论，树形结构显示回复关系。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1")
    })
    @GetMapping("/post/{postId}")
    public Result<List<CommentVO>> getPostComments(@PathVariable Long postId) {
        List<CommentVO> comments = commentService.getPostComments(postId);
        return Result.success(comments);
    }
    
    /**
     * 获取帖子评论列表（分页）
     */
    @Operation(summary = "获取帖子评论列表（分页）", description = "获取指定帖子的评论列表，支持分页。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1"),
            @Parameter(name = "page", description = "页码（从1开始）", example = "1"),
            @Parameter(name = "size", description = "每页条数", example = "10")
    })
    @GetMapping("/list")
    public Result<Map<String, Object>> listComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> pageResult = commentService.listCommentsByPostId(postId, page, size);
        return Result.success(pageResult);
    }
    
    /**
     * 点赞/取消点赞评论 - 切换评论的点赞状态
     */
    @Operation(summary = "点赞/取消点赞评论", description = "对评论进行点赞或取消点赞。")
    @Parameters({
            @Parameter(name = "commentId", description = "评论会idId", required = true, example = "1")
    })
    @PostMapping("/{commentId}/like")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> toggleLike(@PathVariable Long commentId) {
        boolean liked = commentService.toggleLike(commentId);
        return Result.success(liked);
    }
}
