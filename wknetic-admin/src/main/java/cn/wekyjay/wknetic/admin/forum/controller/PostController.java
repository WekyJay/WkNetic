package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.PostService;
import cn.wekyjay.wknetic.common.model.dto.CreatePostDTO;
import cn.wekyjay.wknetic.common.model.dto.UpdatePostDTO;
import cn.wekyjay.wknetic.common.model.vo.PostDetailVO;
import cn.wekyjay.wknetic.common.model.vo.PostVO;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
     * 创建帖子 - 创建一个新的论坛帖子
     */
    @Operation(summary = "创建帖子", description = "创建一个新的论坛帖子。需要提供标题、内容、所属分类等信息。")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createPost(@Valid @RequestBody CreatePostDTO dto) {
        Long postId = postService.createPost(dto);
        return Result.success(postId);
    }
    
    /**
     * 更新帖子 - 修改已发布的帖子内容
     */
    @Operation(summary = "更新帖子", description = "修改帖子的标题、内容等信息。只有帖子作者或管理员可以修改。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1")
    })
    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> updatePost(@PathVariable Long postId, @Valid @RequestBody UpdatePostDTO dto) {
        postService.updatePost(postId, dto);
        return Result.success();
    }
    
    /**
     * 删除帖子 - 软删除帖子（标记为已删除）
     */
    @Operation(summary = "删除帖子", description = "删除指定的帖子。只有帖子作者或管理员可以删除。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1")
    })
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return Result.success();
    }
    
    /**
     * 获取帖子详情 - 获取单个帖子的完整信息
     */
    @Operation(summary = "获取帖子详情", description = "获取指定帖子的完整信息，包括作者、创建时间、评论数、点赞数等。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1")
    })
    @GetMapping("/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        PostDetailVO post = postService.getPostDetail(postId);
        return Result.success(post);
    }
    
    /**
     * 获取帖子列表 - 分页查询帖子列表
     */
    @Operation(summary = "获取帖子列表", description = "分页获取帖子列表，可按分类和状态筛选。")
    @Parameters({
            @Parameter(name = "page", description = "页码", required = true, example = "1"),
            @Parameter(name = "size", description = "每页条数", required = true, example = "20"),
            @Parameter(name = "topicId", description = "分类ID（可选）", example = "1"),
            @Parameter(name = "status", description = "状态：0=草稿，1=已发布，2=审核中，3=已拒绝（可选）", example = "1")
    })
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
     * 点赞/取消点赞帖子 - 切换帖子的点赞状态
     */
    @Operation(summary = "点赞/取消点赞帖子", description = "对帖子进行点赞或取消点赞。返回值为true表示点赞成功，false表示取消点赞。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1")
    })
    @PostMapping("/{postId}/like")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> toggleLike(@PathVariable Long postId) {
        boolean liked = postService.toggleLike(postId);
        return Result.success(liked);
    }
}
