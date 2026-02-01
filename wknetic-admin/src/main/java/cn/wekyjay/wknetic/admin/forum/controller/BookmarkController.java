package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.BookmarkService;
import cn.wekyjay.wknetic.common.model.dto.BookmarkCategoryDTO;
import cn.wekyjay.wknetic.common.model.entity.BookmarkCategory;
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

import java.util.List;

/**
 * 收藏Controller
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Tag(name = "收藏管理", description = "收藏相关接口")
@RestController
@RequestMapping("/api/v1/bookmark")
@RequiredArgsConstructor
public class BookmarkController {
    
    private final BookmarkService bookmarkService;
    
    /**
     * 收藏/取消收藏帖子 - 切换帖子收藏状态
     */
    @Operation(summary = "收藏/取消收藏帖子", description = "收藏或取消收藏指定帖子。可指定收藏到指定类或默认收藏夹。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1"),
            @Parameter(name = "categoryId", description = "收藏夹ID（可选）", example = "1")
    })
    @PostMapping("/post/{postId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> toggleBookmark(
            @PathVariable Long postId,
            @RequestParam(required = false) Long categoryId) {
        boolean bookmarked = bookmarkService.toggleBookmark(postId, categoryId);
        return Result.success(bookmarked);
    }
    
    /**
     * 移动收藏到指定收藏夹 - 转移已收藏的帖子
     */
    @Operation(summary = "移动收藏到指定收藏夹", description = "将已收藏的帖子移到其他收藏夹。")
    @Parameters({
            @Parameter(name = "postId", description = "帖子ID", required = true, example = "1"),
            @Parameter(name = "categoryId", description = "收藏夹ID", required = true, example = "2")
    })
    @PutMapping("/post/{postId}/move")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> moveBookmark(
            @PathVariable Long postId,
            @RequestParam Long categoryId) {
        bookmarkService.moveBookmark(postId, categoryId);
        return Result.success();
    }
    
    /**
     * 获取用户的收藏列表 - 收藏列表分页查询
     */
    @Operation(summary = "获取用户的收藏列表", description = "获取当前用户的收藏列表，可按收藏夹筛选。")
    @Parameters({
            @Parameter(name = "page", description = "页码", example = "1"),
            @Parameter(name = "size", description = "每页条数", example = "20"),
            @Parameter(name = "categoryId", description = "收藏夹ID（可选）", example = "1")
    })
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public Result<IPage<PostVO>> getUserBookmarks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId) {
        IPage<PostVO> bookmarks = bookmarkService.getUserBookmarks(page, size, categoryId);
        return Result.success(bookmarks);
    }
    
    /**
     * 创建收藏夹 - 新建收藏分类
     */
    @Operation(summary = "创建收藏夹", description = "创建一个新的收藏夹或收藏分类。")
    @PostMapping("/category")
    @PreAuthorize("hasRole('USER')")
    public Result<Long> createCategory(@Valid @RequestBody BookmarkCategoryDTO dto) {
        Long categoryId = bookmarkService.createCategory(dto);
        return Result.success(categoryId);
    }
    
    /**
     * 更新收藏夹 - 修改收藏夹信息
     */
    @Operation(summary = "更新收藏夹", description = "修改收藏夹的名称和简介。")
    @Parameters({
            @Parameter(name = "categoryId", description = "收藏夹ID", required = true, example = "1")
    })
    @PutMapping("/category/{categoryId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody BookmarkCategoryDTO dto) {
        bookmarkService.updateCategory(categoryId, dto);
        return Result.success();
    }
    
    /**
     * 删除收藏夹 - 软删除收藏夹
     */
    @Operation(summary = "删除收藏夹", description = "删除指定的收藏夹。删除收藏夹時，其下收藏的帖子将移到默认收藏夹。")
    @Parameters({
            @Parameter(name = "categoryId", description = "收藏夹ID", required = true, example = "1")
    })
    @DeleteMapping("/category/{categoryId}")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        bookmarkService.deleteCategory(categoryId);
        return Result.success();
    }
    
    /**
     * 获取用户的收藏夹列表 - 查询收藏夹
     */
    @Operation(summary = "获取用户的收藏夹列表", description = "获取当前用户的所有收藏夹。")
    @GetMapping("/category/list")
    @PreAuthorize("hasRole('USER')")
    public Result<List<BookmarkCategory>> getUserCategories() {
        List<BookmarkCategory> categories = bookmarkService.getUserCategories();
        return Result.success(categories);
    }
}
