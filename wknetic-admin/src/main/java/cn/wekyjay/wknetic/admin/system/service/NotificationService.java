package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.common.mapper.NotificationMapper;
import cn.wekyjay.wknetic.common.model.entity.Notification;
import cn.wekyjay.wknetic.common.model.vo.NotificationVO;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {
    
    private final NotificationMapper notificationMapper;
    
    /**
     * 创建通知
     *
     * @param userId 接收用户ID
     * @param type 通知类型
     * @param title 标题
     * @param content 内容
     * @param relatedId 关联对象ID
     * @param relatedType 关联对象类型
     * @param senderId 发送者ID
     */
    public void createNotification(Long userId, String type, String title, String content,
                                   Long relatedId, String relatedType, Long senderId) {
        // 不给自己发通知
        if (userId.equals(senderId)) {
            return;
        }
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setSenderId(senderId);
        notification.setIsRead(false);
        
        notificationMapper.insert(notification);
        log.debug("创建通知: userId={}, type={}", userId, type);
    }
    
    /**
     * 获取用户通知列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 通知列表
     */
    public IPage<NotificationVO> getUserNotifications(int page, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        Page<Notification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        
        IPage<Notification> notificationPage = notificationMapper.selectPage(pageParam, wrapper);
        
        // TODO: 转换为VO，加载发送者信息
        return notificationPage.convert(notification -> {
            NotificationVO vo = new NotificationVO();
            vo.setNotificationId(notification.getNotificationId());
            vo.setType(notification.getType());
            vo.setTitle(notification.getTitle());
            vo.setContent(notification.getContent());
            vo.setRelatedId(notification.getRelatedId());
            vo.setRelatedType(notification.getRelatedType());
            vo.setIsRead(notification.getIsRead());
            vo.setCreateTime(notification.getCreateTime());
            vo.setReadTime(notification.getReadTime());
            return vo;
        });
    }
    
    /**
     * 获取未读通知数量
     *
     * @return 未读数量
     */
    public int getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationMapper.countUnreadByUserId(userId);
    }
    
    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new RuntimeException("通知不存在或无权访问");
        }
        
        if (!notification.getIsRead()) {
            Notification update = new Notification();
            update.setNotificationId(notificationId);
            update.setIsRead(true);
            update.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(update);
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        List<Notification> unreadNotifications = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false)
        );
        
        for (Notification notification : unreadNotifications) {
            Notification update = new Notification();
            update.setNotificationId(notification.getNotificationId());
            update.setIsRead(true);
            update.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(update);
        }
        
        log.info("用户{}标记所有通知为已读，共{}条", userId, unreadNotifications.size());
    }
}
