package cn.wekyjay.wknetic.community.event.user;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 用户登录事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class UserLoginEvent extends BaseEvent {
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * 用户名
     */
    private final String username;
    
    /**
     * 登录IP
     */
    private final String ipAddress;
    
    /**
     * User-Agent
     */
    private final String userAgent;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress 登录IP
     * @param userAgent User-Agent
     */
    public UserLoginEvent(Object source, Long userId, String username, String ipAddress, String userAgent) {
        super(source, userId, "USER_LOGIN");
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}
