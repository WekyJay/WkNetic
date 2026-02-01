package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.TopicService;
import cn.wekyjay.wknetic.common.model.vo.TopicVO;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 板块Controller
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Tag(name = "板块管理", description = "板块相关接口")
@RestController
@RequestMapping("/api/v1/topic")
@RequiredArgsConstructor
public class TopicController {
    
    private final TopicService topicService;
    
    /**
     * 创建板块 - 新增一个论坛板块
     */
    @Operation(summary = "创建板块", description = "新增一个论坛板块（分类）。需要管理员权限。")
    @Parameters({
            @Parameter(name = "name", description = "板块名称", required = true, example = "技术算法"),
            @Parameter(name = "description", description = "板块描述（可选）", example = "讨论技术轮数据结构等"),
            @Parameter(name = "icon", description = "板块图片或图一（可选）", example = "tech-icon")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createTopic(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String icon) {
        Long topicId = topicService.createTopic(name, description, icon);
        return Result.success(topicId);
    }
    
    /**
     * 更新板块 - 修改板块信息
     */
    @Operation(summary = "更新板块", description = "修改板块的名称、描述、图一等信息。")
    @Parameters({
            @Parameter(name = "topicId", description = "板块ID", required = true, example = "1"),
            @Parameter(name = "name", description = "板块名称（可选）", example = "技术算法"),
            @Parameter(name = "description", description = "板块描述（可选）"),
            @Parameter(name = "icon", description = "板块图一（可选）")
    })
    @PutMapping("/{topicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateTopic(
            @PathVariable Long topicId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String icon) {
        topicService.updateTopic(topicId, name, description, icon);
        return Result.success();
    }
    
    /**
     * 删除板块 - 管理员才能删除板块
     */
    @Operation(summary = "删除板块", description = "管理员可以删除板块，删除后板块下的内容将被不可恢地删除。")
    @Parameters({
            @Parameter(name = "topicId", description = "板块ID", required = true, example = "1")
    })
    @DeleteMapping("/{topicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteTopic(@PathVariable Long topicId) {
        topicService.deleteTopic(topicId);
        return Result.success();
    }
    
    /**
     * 获取板块详情 - 查看板块的不不薦信息和统计
     */
    @Operation(summary = "获取板块详情", description = "获取指定板块的详细信息，包括板块描述、帖子数、最新帖子等。")
    @Parameters({
            @Parameter(name = "topicId", description = "板块ID", required = true, example = "1")
    })
    @GetMapping("/{topicId}")
    public Result<TopicVO> getTopicDetail(@PathVariable Long topicId) {
        TopicVO topic = topicService.getTopicDetail(topicId);
        return Result.success(topic);
    }
    
    /**
     * 获取所有板块列表 - 用于展示所有可用的板块
     */
    @Operation(summary = "获取所有板块列表", description = "获取已启用的所有板块，不进行分页。用于前端鼓动板块选择器。")
    @GetMapping("/all")
    public Result<List<TopicVO>> listAllTopics() {
        List<TopicVO> topics = topicService.listAllTopics();
        return Result.success(topics);
    }
    
    /**
     * 分页获取板块列表
     */
    @Operation(summary = "分页获取板块列表")
    @GetMapping("/list")
    public Result<IPage<TopicVO>> listTopics(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<TopicVO> topics = topicService.listTopics(page, size);
        return Result.success(topics);
    }
}
