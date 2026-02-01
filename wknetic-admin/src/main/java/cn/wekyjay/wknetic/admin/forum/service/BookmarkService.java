package cn.wekyjay.wknetic.admin.forum.service;

import cn.wekyjay.wknetic.common.mapper.BookmarkCategoryMapper;
import cn.wekyjay.wknetic.common.mapper.ForumPostMapper;
import cn.wekyjay.wknetic.common.mapper.PostBookmarkMapper;
import cn.wekyjay.wknetic.common.model.dto.BookmarkCategoryDTO;
import cn.wekyjay.wknetic.common.model.entity.BookmarkCategory;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import cn.wekyjay.wknetic.common.model.entity.PostBookmark;
import cn.wekyjay.wknetic.common.model.vo.PostVO;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏Service
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Service
@RequiredArgsConstructor
public class BookmarkService {
    
    private final PostBookmarkMapper bookmarkMapper;
    private final BookmarkCategoryMapper categoryMapper;
    private final ForumPostMapper postMapper;
    
    /**
     * 收藏/取消收藏帖子
     *
     * @param postId 帖子ID
     * @param categoryId 收藏夹ID（可选）
     * @return true=已收藏，false=已取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleBookmark(Long postId, Long categoryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        
        // 检查帖子是否存在
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        // 查询是否已收藏
        PostBookmark existingBookmark = bookmarkMapper.selectOne(new LambdaQueryWrapper<PostBookmark>()
                .eq(PostBookmark::getUserId, userId)
                .eq(PostBookmark::getPostId, postId));
        
        if (existingBookmark != null) {
            // 已收藏，取消收藏
            bookmarkMapper.deleteById(existingBookmark.getId());
            return false;
        } else {
            // 未收藏，添加收藏
            // 如果没有指定收藏夹，使用默认收藏夹
            if (categoryId == null) {
                categoryId = getOrCreateDefaultCategory(userId);
            } else {
                // 验证收藏夹是否属于当前用户
                BookmarkCategory category = categoryMapper.selectById(categoryId);
                if (category == null || !category.getUserId().equals(userId)) {
                    throw new RuntimeException("收藏夹不存在或无权访问");
                }
            }
            
            PostBookmark bookmark = new PostBookmark();
            bookmark.setUserId(userId);
            bookmark.setPostId(postId);
            bookmark.setCategoryId(categoryId);
            bookmark.setCreateTime(LocalDateTime.now());
            
            bookmarkMapper.insert(bookmark);
            return true;
        }
    }
    
    /**
     * 移动收藏到指定收藏夹
     *
     * @param postId 帖子ID
     * @param categoryId 目标收藏夹ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveBookmark(Long postId, Long categoryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        
        // 验证收藏夹是否属于当前用户
        BookmarkCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new RuntimeException("收藏夹不存在或无权访问");
        }
        
        // 更新收藏的收藏夹
        LambdaUpdateWrapper<PostBookmark> updateWrapper = new LambdaUpdateWrapper<PostBookmark>()
                .eq(PostBookmark::getUserId, userId)
                .eq(PostBookmark::getPostId, postId)
                .set(PostBookmark::getCategoryId, categoryId);
        
        int updated = bookmarkMapper.update(null, updateWrapper);
        if (updated == 0) {
            throw new RuntimeException("收藏不存在");
        }
    }
    
    /**
     * 获取用户的收藏列表
     *
     * @param page 页码
     * @param size 每页数量
     * @param categoryId 收藏夹ID（可选）
     * @return 收藏列表
     */
    public IPage<PostVO> getUserBookmarks(int page, int size, Long categoryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        
        Page<PostBookmark> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PostBookmark> queryWrapper = new LambdaQueryWrapper<PostBookmark>()
                .eq(PostBookmark::getUserId, userId)
                .orderByDesc(PostBookmark::getCreateTime);
        
        if (categoryId != null) {
            queryWrapper.eq(PostBookmark::getCategoryId, categoryId);
        }
        
        IPage<PostBookmark> bookmarkPage = bookmarkMapper.selectPage(pageParam, queryWrapper);
        
        // 转换为PostVO
        Page<PostVO> voPage = new Page<>(page, size, bookmarkPage.getTotal());
        List<PostVO> postVOList = bookmarkPage.getRecords().stream()
                .map(bookmark -> {
                    ForumPost post = postMapper.selectById(bookmark.getPostId());
                    if (post == null) {
                        return null;
                    }
                    // TODO: 转换为PostVO，需要填充作者信息等
                    PostVO vo = new PostVO();
                    vo.setPostId(post.getPostId());
                    vo.setTitle(post.getTitle());
                    vo.setExcerpt(post.getExcerpt());
                    vo.setViewCount(post.getViewCount());
                    vo.setLikeCount(post.getLikeCount());
                    vo.setCommentCount(post.getCommentCount());
                    vo.setCreateTime(post.getCreateTime());
                    vo.setIsBookmarked(true);
                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
        
        voPage.setRecords(postVOList);
        return voPage;
    }
    
    /**
     * 创建收藏夹
     *
     * @param dto 收藏夹DTO
     * @return 收藏夹ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(BookmarkCategoryDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        
        // 检查名称是否已存在
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<BookmarkCategory>()
                .eq(BookmarkCategory::getUserId, userId)
                .eq(BookmarkCategory::getCategoryName, dto.getCategoryName()));
        if (count > 0) {
            throw new RuntimeException("收藏夹名称已存在");
        }
        
        BookmarkCategory category = new BookmarkCategory();
        category.setUserId(userId);
        category.setCategoryName(dto.getCategoryName());
        category.setDescription(dto.getDescription());
        category.setIsDefault(false);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        
        categoryMapper.insert(category);
        return category.getCategoryId();
    }
    
    /**
     * 更新收藏夹
     *
     * @param categoryId 收藏夹ID
     * @param dto 收藏夹DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long categoryId, BookmarkCategoryDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        
        BookmarkCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new RuntimeException("收藏夹不存在或无权访问");
        }
        
        if (category.getIsDefault()) {
            throw new RuntimeException("默认收藏夹不能修改名称");
        }
        
        // 检查名称是否已存在
        if (!category.getCategoryName().equals(dto.getCategoryName())) {
            Long count = categoryMapper.selectCount(new LambdaQueryWrapper<BookmarkCategory>()
                    .eq(BookmarkCategory::getUserId, userId)
                    .eq(BookmarkCategory::getCategoryName, dto.getCategoryName())
                    .ne(BookmarkCategory::getCategoryId, categoryId));
            if (count > 0) {
                throw new RuntimeException("收藏夹名称已存在");
            }
        }
        
        LambdaUpdateWrapper<BookmarkCategory> updateWrapper = new LambdaUpdateWrapper<BookmarkCategory>()
                .eq(BookmarkCategory::getCategoryId, categoryId)
                .set(BookmarkCategory::getCategoryName, dto.getCategoryName())
                .set(dto.getDescription() != null, BookmarkCategory::getDescription, dto.getDescription())
                .set(BookmarkCategory::getUpdateTime, LocalDateTime.now());
        
        categoryMapper.update(null, updateWrapper);
    }
    
    /**
     * 删除收藏夹
     *
     * @param categoryId 收藏夹ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        
        BookmarkCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new RuntimeException("收藏夹不存在或无权访问");
        }
        
        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new RuntimeException("默认收藏夹不能删除");
        }
        
        // 将该收藏夹下的收藏移动到默认收藏夹
        Long defaultCategoryId = getOrCreateDefaultCategory(userId);
        LambdaUpdateWrapper<PostBookmark> updateWrapper = new LambdaUpdateWrapper<PostBookmark>()
                .eq(PostBookmark::getUserId, userId)
                .eq(PostBookmark::getCategoryId, categoryId)
                .set(PostBookmark::getCategoryId, defaultCategoryId);
        bookmarkMapper.update(null, updateWrapper);
        
        // 删除收藏夹
        categoryMapper.deleteById(categoryId);
    }
    
    /**
     * 获取用户的收藏夹列表
     *
     * @return 收藏夹列表
     */
    public List<BookmarkCategory> getUserCategories() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        
        return categoryMapper.selectList(new LambdaQueryWrapper<BookmarkCategory>()
                .eq(BookmarkCategory::getUserId, userId)
                .orderByDesc(BookmarkCategory::getIsDefault)
                .orderByAsc(BookmarkCategory::getCreateTime));
    }
    
    /**
     * 获取或创建默认收藏夹
     */
    private Long getOrCreateDefaultCategory(Long userId) {
        BookmarkCategory defaultCategory = categoryMapper.selectOne(new LambdaQueryWrapper<BookmarkCategory>()
                .eq(BookmarkCategory::getUserId, userId)
                .eq(BookmarkCategory::getIsDefault, true));
        
        if (defaultCategory == null) {
            defaultCategory = new BookmarkCategory();
            defaultCategory.setUserId(userId);
            defaultCategory.setCategoryName("默认收藏夹");
            defaultCategory.setDescription("系统自动创建的默认收藏夹");
            defaultCategory.setIsDefault(true);
            defaultCategory.setCreateTime(LocalDateTime.now());
            defaultCategory.setUpdateTime(LocalDateTime.now());
            
            categoryMapper.insert(defaultCategory);
        }
        
        return defaultCategory.getCategoryId();
    }
}
