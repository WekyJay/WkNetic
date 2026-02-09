package cn.wekyjay.wknetic.common.model.dto;

import cn.wekyjay.wknetic.common.enums.DeviceFlowStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 设备流状态数据传输对象
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceFlowStateDTO {
    
    /**
     * 设备码（后端标识）
     */
    private String deviceCode;
    
    /**
     * 用户码（显示给用户）
     */
    private String userCode;
    
    /**
     * 验证URI
     */
    private String verificationUri;
    
    /**
     * 完整的验证URI（带用户码）
     */
    private String verificationUriComplete;
    
    /**
     * 当前状态
     */
    private DeviceFlowStatus status;
    
    /**
     * 状态描述
     */
    private String statusDescription;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 轮询间隔（秒）
     */
    private Integer interval;
    
    /**
     * 关联的用户ID
     */
    private Long userId;
    
    /**
     * Microsoft访问令牌（仅在认证成功后返回）
     */
    private String microsoftAccessToken;
    
    /**
     * Minecraft UUID（仅在认证成功后返回）
     */
    private String minecraftUuid;
    
    /**
     * Minecraft用户名（仅在认证成功后返回）
     */
    private String minecraftUsername;
    
    /**
     * 错误信息（仅在状态为ERROR时返回）
     */
    private String errorMessage;
    
    /**
     * 是否为终端状态
     */
    private Boolean terminal;


    /**
     * 是否已过期
     */
    private boolean expired;
    
    /**
     * 剩余时间（秒）
     */
    private Long timeRemaining;
    
    public DeviceFlowStateDTO() {
        this.createdAt = LocalDateTime.now();
        this.terminal = false;
    }
    
    public void setStatus(DeviceFlowStatus status) {
        this.status = status;
        this.statusDescription = status != null ? status.getDescription() : null;
        this.terminal = status != null && status.isTerminal();
    }
    
    /**
     * 计算剩余时间
     */
    public void calculateTimeRemaining() {
        if (expiresAt != null) {
            long remaining = expiresAt.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            this.timeRemaining = Math.max(0, remaining);
        } else {
            this.timeRemaining = 0L;
        }
    }
    
    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
}