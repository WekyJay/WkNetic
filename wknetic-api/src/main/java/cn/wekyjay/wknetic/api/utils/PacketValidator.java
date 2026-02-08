package cn.wekyjay.wknetic.api.utils;

import cn.wekyjay.wknetic.api.model.packet.BasePacket;

/**
 * Packet验证工具类
 * 提供静态方法用于验证Packet数据的完整性和有效性
 * 注意：这是一个轻量级验证工具，不依赖Hibernate Validator
 */
public class PacketValidator {
    
    private PacketValidator() {
        // 工具类，禁止实例化
    }
    
    /**
     * 验证Packet数据是否有效
     * @param packet 要验证的Packet
     * @return 如果Packet有效返回true，否则返回false
     */
    public static boolean isValid(BasePacket packet) {
        return validate(packet).isValid();
    }
    
    /**
     * 验证Packet数据并返回详细的验证结果
     * @param packet 要验证的Packet
     * @return 验证结果对象，包含是否有效和错误信息
     */
    public static ValidationResult validate(BasePacket packet) {
        if (packet == null) {
            return ValidationResult.failure("Packet不能为空");
        }
        
        // 验证Packet类型
        if (packet.getType() == null) {
            return ValidationResult.failure("Packet类型不能为空");
        }
        
        // 验证Token
        if (!isValidToken(packet.getToken())) {
            return ValidationResult.failure("Token格式无效（长度必须在32-64字符之间）");
        }
        
        // 验证协议版本
        if (!isSupportedProtocolVersion(packet.getProtocolVersion())) {
            return ValidationResult.failure("不支持的协议版本：" + packet.getProtocolVersion());
        }
        
        // 验证时间戳
        if (!isValidTimestamp(packet.getTimestamp())) {
            return ValidationResult.failure("时间戳不在有效范围内");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 验证Packet的Token格式是否正确
     * @param token 要验证的Token
     * @return 如果Token格式正确返回true，否则返回false
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // Token长度必须在32-64字符之间
        int length = token.length();
        return length >= 32 && length <= 64;
    }
    
    /**
     * 验证Packet的时间戳是否在合理范围内
     * @param timestamp 时间戳
     * @return 如果时间戳在合理范围内返回true，否则返回false
     */
    public static boolean isValidTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long maxAllowedTime = currentTime + 300000; // 允许未来5分钟的时间戳
        long minAllowedTime = currentTime - 86400000; // 允许过去24小时的时间戳
        
        return timestamp >= minAllowedTime && timestamp <= maxAllowedTime;
    }
    
    /**
     * 验证Packet的协议版本是否支持
     * @param protocolVersion 协议版本
     * @return 如果协议版本支持返回true，否则返回false
     */
    public static boolean isSupportedProtocolVersion(int protocolVersion) {
        // 当前支持协议版本1-3
        return protocolVersion >= 1 && protocolVersion <= 3;
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public String getFormattedErrorMessage() {
            if (valid) {
                return "验证通过";
            }
            return "验证失败: " + errorMessage;
        }
    }
}
