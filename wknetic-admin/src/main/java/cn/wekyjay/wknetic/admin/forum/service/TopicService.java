package cn.wekyjay.wknetic.admin.forum.service;

import cn.wekyjay.wknetic.common.mapper.ForumTopicMapper;
import cn.wekyjay.wknetic.common.model.entity.ForumTopic;
import cn.wekyjay.wknetic.common.model.vo.TopicVO;
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
 * 板块Service
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Service
@RequiredArgsConstructor
public class TopicService {
    
    private final ForumTopicMapper topicMapper;
    
    /**
     * 创建板块
     *
     * @param name 板块名称
     * @param description 板块描述
     * @param icon 板块图标
     * @return 板块ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTopic(String name, String description, String icon) {
        if (!SecurityUtils.isAdmin()) {
            throw new RuntimeException("只有管理员可以创建板块");
        }
        
        // 检查名称是否已存在
        Long count = topicMapper.selectCount(new LambdaQueryWrapper<ForumTopic>()
                .eq(ForumTopic::getTopicName, name));
        if (count > 0) {
            throw new RuntimeException("板块名称已存在");
        }
        
        ForumTopic topic = new ForumTopic();
        topic.setTopicName(name);
        topic.setTopicDesc(description);
        topic.setIcon(icon);
        topic.setPostCount(0);
        topic.setStatus(1);
        topic.setCreateTime(LocalDateTime.now());
        topic.setUpdateTime(LocalDateTime.now());
        
        topicMapper.insert(topic);
        return topic.getTopicId();
    }
    
    /**
     * 更新板块
     *
     * @param topicId 板块ID
     * @param name 板块名称
     * @param description 板块描述
     * @param icon 板块图标
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(Long topicId, String name, String description, String icon) {
        if (!SecurityUtils.isAdmin()) {
            throw new RuntimeException("只有管理员可以更新板块");
        }
        
        ForumTopic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new RuntimeException("板块不存在");
        }
        
        // 如果修改名称，检查是否重复
        if (name != null && !name.equals(topic.getTopicName())) {
            Long count = topicMapper.selectCount(new LambdaQueryWrapper<ForumTopic>()
                    .eq(ForumTopic::getTopicName, name)
                    .ne(ForumTopic::getTopicId, topicId));
            if (count > 0) {
                throw new RuntimeException("板块名称已存在");
            }
        }
        
        LambdaUpdateWrapper<ForumTopic> updateWrapper = new LambdaUpdateWrapper<ForumTopic>()
                .eq(ForumTopic::getTopicId, topicId)
                .set(name != null, ForumTopic::getTopicName, name)
                .set(description != null, ForumTopic::getTopicDesc, description)
                .set(icon != null, ForumTopic::getIcon, icon)
                .set(ForumTopic::getUpdateTime, LocalDateTime.now());
        
        topicMapper.update(null, updateWrapper);
    }
    
    /**
     * 删除板块
     *
     * @param topicId 板块ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long topicId) {
        if (!SecurityUtils.isAdmin()) {
            throw new RuntimeException("只有管理员可以删除板块");
        }
        
        ForumTopic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new RuntimeException("板块不存在");
        }
        
        // 检查板块下是否还有帖子
        if (topic.getPostCount() > 0) {
            throw new RuntimeException("板块下还有帖子，无法删除");
        }
        
        topicMapper.deleteById(topicId);
    }
    
    /**
     * 获取板块详情
     *
     * @param topicId 板块ID
     * @return 板块VO
     */
    public TopicVO getTopicDetail(Long topicId) {
        ForumTopic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new RuntimeException("板块不存在");
        }
        
        return convertToVO(topic);
    }
    
    /**
     * 获取所有板块列表
     *
     * @return 板块列表
     */
    public List<TopicVO> listAllTopics() {
        List<ForumTopic> topics = topicMapper.selectList(new LambdaQueryWrapper<ForumTopic>()
                .eq(ForumTopic::getStatus, 1)
                .orderByDesc(ForumTopic::getPostCount));
        
        return topics.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 分页获取板块列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 板块分页列表
     */
    public IPage<TopicVO> listTopics(int page, int size) {
        Page<ForumTopic> pageParam = new Page<>(page, size);
        IPage<ForumTopic> topicPage = topicMapper.selectPage(pageParam, new LambdaQueryWrapper<ForumTopic>()
                .eq(ForumTopic::getStatus, 1)
                .orderByDesc(ForumTopic::getPostCount));
        
        Page<TopicVO> voPage = new Page<>(page, size, topicPage.getTotal());
        voPage.setRecords(topicPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }
    
    /**
     * 增加板块帖子数
     *
     * @param topicId 板块ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementPostCount(Long topicId) {
        topicMapper.incrementPostCount(topicId);
    }
    
    /**
     * 减少板块帖子数
     *
     * @param topicId 板块ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void decrementPostCount(Long topicId) {
        topicMapper.decrementPostCount(topicId);
    }
    
    /**
     * 转换为VO
     */
    private TopicVO convertToVO(ForumTopic topic) {
        TopicVO vo = new TopicVO();
        vo.setTopicId(topic.getTopicId());
        vo.setTopicName(topic.getTopicName());
        vo.setTopicDesc(topic.getTopicDesc());
        vo.setIcon(topic.getIcon());
        vo.setPostCount(topic.getPostCount());
        vo.setCreateTime(topic.getCreateTime());
        return vo;
    }
}
