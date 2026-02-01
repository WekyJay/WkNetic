package cn.wekyjay.wknetic.community.event.user;

import cn.wekyjay.wknetic.community.event.BaseEvent;
import lombok.Getter;

/**
 * 用户注册事件
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Getter
public class UserRegisterEvent extends BaseEvent {
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * 用户名
     */
    private final String username;
    
    /**
     * 邮箱
     */
    private final String email;
    
    /**
     * 注册IP
     */
    private final String ipAddress;
    
    /**
     * 构造函数
     *
     * @param source 事件源
     * @param userId 用户ID
     * @param username 用户名
     * @param email 邮箱
     * @param ipAddress 注册IP
     */
    public UserRegisterEvent(Object source, Long userId, String username, String email, String ipAddress) {
        super(source, userId, "USER_REGISTER");
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.ipAddress = ipAddress;
    }
}
