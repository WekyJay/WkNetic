package cn.wekyjay.wknetic.admin.forum.controller;

import cn.wekyjay.wknetic.admin.forum.service.TopicService;
import cn.wekyjay.wknetic.common.model.vo.TopicVO;
import cn.wekyjay.wknetic.common.model.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
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
     * 创建板块
     */
    @Operation(summary = "创建板块")
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
     * 更新板块
     */
    @Operation(summary = "更新板块")
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
     * 删除板块
     */
    @Operation(summary = "删除板块")
    @DeleteMapping("/{topicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteTopic(@PathVariable Long topicId) {
        topicService.deleteTopic(topicId);
        return Result.success();
    }
    
    /**
     * 获取板块详情
     */
    @Operation(summary = "获取板块详情")
    @GetMapping("/{topicId}")
    public Result<TopicVO> getTopicDetail(@PathVariable Long topicId) {
        TopicVO topic = topicService.getTopicDetail(topicId);
        return Result.success(topic);
    }
    
    /**
     * 获取所有板块列表
     */
    @Operation(summary = "获取所有板块列表")
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
