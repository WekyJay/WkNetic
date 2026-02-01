package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.PostService;
import cn.wekyjay.wknetic.common.model.dto.CreatePostDTO;
import cn.wekyjay.wknetic.common.model.dto.UpdatePostDTO;
import cn.wekyjay.wknetic.common.model.vo.PostDetailVO;
import cn.wekyjay.wknetic.common.model.vo.PostVO;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子Controller
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Tag(name = "帖子管理", description = "帖子相关接口")
@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    
    /**
     * 创建帖子
     */
    @Operation(summary = "创建帖子")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createPost(@Valid @RequestBody CreatePostDTO dto) {
        Long postId = postService.createPost(dto);
        return Result.success(postId);
    }
    
    /**
     * 更新帖子
     */
    @Operation(summary = "更新帖子")
    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> updatePost(@PathVariable Long postId, @Valid @RequestBody UpdatePostDTO dto) {
        postService.updatePost(postId, dto);
        return Result.success();
    }
    
    /**
     * 删除帖子
     */
    @Operation(summary = "删除帖子")
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return Result.success();
    }
    
    /**
     * 获取帖子详情
     */
    @Operation(summary = "获取帖子详情")
    @GetMapping("/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        PostDetailVO post = postService.getPostDetail(postId);
        return Result.success(post);
    }
    
    /**
     * 获取帖子列表
     */
    @Operation(summary = "获取帖子列表")
    @GetMapping("/list")
    public Result<IPage<PostVO>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Integer status) {
        IPage<PostVO> posts = postService.listPosts(page, size, topicId, status);
        return Result.success(posts);
    }
    
    /**
     * 点赞/取消点赞帖子
     */
    @Operation(summary = "点赞/取消点赞帖子")
    @PostMapping("/{postId}/like")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> toggleLike(@PathVariable Long postId) {
        boolean liked = postService.toggleLike(postId);
        return Result.success(liked);
    }
}
