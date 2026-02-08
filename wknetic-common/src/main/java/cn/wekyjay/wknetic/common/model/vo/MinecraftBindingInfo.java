package cn.wekyjay.wknetic.common.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Minecraft绑定信息
 */
@Data
public class MinecraftBindingInfo {
    /** Minecraft UUID */
    private String minecraftUuid;
    
    /** Minecraft用户名 */
    private String minecraftUsername;
    
    /** 绑定时间 */
    private LocalDateTime bindTime;
    
    /** 是否已绑定 */
    private boolean bound;
    
    /** 绑定状态描述 */
    private String status;
    
    /** Minecraft头像URL */
    private String avatarUrl;
}