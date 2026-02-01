package cn.wekyjay.wknetic.community.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 事件发布工具类
 * 封装Spring ApplicationEventPublisher，提供统一的事件发布接口
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * 发布事件
     *
     * @param event 事件对象
     */
    public void publishEvent(BaseEvent event) {
        try {
            // log.debug("Publishing event: {}", event);
            applicationEventPublisher.publishEvent(event);
            // log.debug("Event published successfully: {}", event.getClass().getSimpleName());
        } catch (Exception e) {
            // log.error("Failed to publish event: {}", event, e);
            // 不抛出异常，避免影响主业务流程
        }
    }
    
    /**
     * 异步发布事件（实际异步处理由监听器的@Async注解决定）
     * 此方法仅作为语义化接口，提醒开发者此事件适合异步处理
     *
     * @param event 事件对象
     */
    public void publishEventAsync(BaseEvent event) {
        publishEvent(event);
    }
}
