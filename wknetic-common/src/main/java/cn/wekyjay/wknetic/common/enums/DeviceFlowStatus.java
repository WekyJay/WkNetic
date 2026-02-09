package cn.wekyjay.wknetic.common.enums;

/**
 * 设备流状态枚举
 */
public enum DeviceFlowStatus {
    /**
     * 等待用户授权
     */
    PENDING("pending", "等待用户授权"),
    
    /**
     * 用户已授权，等待后端处理
     */
    AUTHORIZED("authorized", "用户已授权"),
    
    /**
     * 处理中 - 正在进行Xbox和Minecraft认证
     */
    PROCESSING("processing", "处理中"),
    
    /**
     * 完成 - 认证成功
     */
    COMPLETED("completed", "认证成功"),
    
    /**
     * 过期 - 设备码已过期
     */
    EXPIRED("expired", "设备码已过期"),
    
    /**
     * 拒绝 - 用户拒绝授权
     */
    DENIED("denied", "用户拒绝授权"),
    
    /**
     * 错误 - 认证过程中发生错误
     */
    ERROR("error", "认证错误");

    private final String code;
    private final String description;

    DeviceFlowStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DeviceFlowStatus fromCode(String code) {
        for (DeviceFlowStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == EXPIRED || this == DENIED || this == ERROR;
    }
}