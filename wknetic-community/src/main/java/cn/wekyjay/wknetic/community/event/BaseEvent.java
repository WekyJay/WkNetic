package cn.wekyjay.wknetic.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 基础事件类
 * 所有自定义事件都应继承此类
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public abstract class BaseEvent extends ApplicationEvent {

    public LocalDateTime getEventTime() {
        return eventTime;
    }
    
    /**
     * 事件时间（LocalDateTime 类型，避免与 ApplicationEvent 的 getTimestamp() 冲突）
     */
    private final LocalDateTime eventTime;
    
    /**
     * 触发事件的用户ID（可选）
     */
    private final Long userId;
    
    /**
     * 事件类型（用于日志和调试）
     */
    private final String eventType;
    
    /**
     * 构造函数
     *
     * @param source 事件源对象
     * @param userId 触发事件的用户ID
     * @param eventType 事件类型
     */
    protected BaseEvent(Object source, Long userId, String eventType) {
        super(source);
        this.eventTime = LocalDateTime.now();
        this.userId = userId;
        this.eventType = eventType;
    }
    
    /**
     * 构造函数（不指定用户ID，用于系统事件）
     *
     * @param source 事件源对象
     * @param eventType 事件类型
     */
    protected BaseEvent(Object source, String eventType) {
        this(source, null, eventType);
    }
    

    @Override
    public String toString() {
        return String.format("%s{eventType='%s', userId=%d, eventTime=%s}",
                getClass().getSimpleName(), eventType, userId, eventTime);
    }
}
