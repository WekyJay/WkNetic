package cn.wekyjay.wknetic.api.example;

import cn.wekyjay.wknetic.api.enums.PacketType;
import cn.wekyjay.wknetic.api.model.packet.BasePacket;
import cn.wekyjay.wknetic.api.utils.PacketValidator;
import cn.wekyjay.wknetic.api.utils.PacketValidator.ValidationResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Packet使用示例类
 * 演示如何使用PacketValidator和PacketUtils工具类
 */
public class PacketExample {
    
    /**
     * 示例Packet类 - 用于演示
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExamplePacket extends BasePacket {
        private String data;
        private int value;
        
        public ExamplePacket(PacketType type, String token, int protocolVersion, String data, int value) {
            this.setType(type);
            this.setToken(token);
            this.setProtocolVersion(protocolVersion);
            this.setTimestamp(System.currentTimeMillis());
            this.data = data;
            this.value = value;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Packet工具类使用示例 ===\n");
        
        // 1. 创建有效的Packet
        ExamplePacket validPacket = new ExamplePacket(
            PacketType.SERVER_LOGIN,
            "1234567890123456789012345678901234567890", // 40字符的token
            1,
            "示例数据",
            100
        );
        
        System.out.println("1. 创建有效的Packet:");
        System.out.println("   Packet类型: " + validPacket.getType());
        System.out.println("   Token长度: " + validPacket.getToken().length());
        System.out.println("   协议版本: " + validPacket.getProtocolVersion());
        System.out.println("   数据: " + validPacket.getData());
        System.out.println("   值: " + validPacket.getValue());
        
        // 2. 使用PacketValidator验证Packet
        System.out.println("\n2. 使用PacketValidator验证Packet:");
        boolean isValid = PacketValidator.isValid(validPacket);
        System.out.println("   isValid() 结果: " + isValid);
        
        ValidationResult validationResult = PacketValidator.validate(validPacket);
        System.out.println("   validate() 结果: " + validationResult.getFormattedErrorMessage());
        
        // 3. 创建无效的Packet进行验证
        System.out.println("\n3. 创建无效的Packet进行验证:");
        ExamplePacket invalidPacket = new ExamplePacket(
            null, // 类型为空
            "short", // token太短
            0, // 协议版本无效
            "无效数据",
            -1
        );
        
        ValidationResult invalidResult = PacketValidator.validate(invalidPacket);
        System.out.println("   无效Packet验证结果: " + invalidResult.getFormattedErrorMessage());
        
        // 4. 验证工具方法
        System.out.println("\n4. 验证工具方法:");
        boolean isTokenValid = PacketValidator.isValidToken(validPacket.getToken());
        System.out.println("   Token验证: " + isTokenValid);
        
        boolean isTimestampValid = PacketValidator.isValidTimestamp(validPacket.getTimestamp());
        System.out.println("   时间戳验证: " + isTimestampValid);
        
        boolean isProtocolSupported = PacketValidator.isSupportedProtocolVersion(validPacket.getProtocolVersion());
        System.out.println("   协议版本支持: " + isProtocolSupported);
        
        // 5. 测试边界情况
        System.out.println("\n5. 测试边界情况:");
        
        // 测试Token边界
        String shortToken = "1234567890123456789012345678901"; // 31字符
        String longToken = "1234567890123456789012345678901234567890123456789012345678901234"; // 64字符
        String tooLongToken = "12345678901234567890123456789012345678901234567890123456789012345"; // 65字符
        
        System.out.println("   短Token验证 (31字符): " + PacketValidator.isValidToken(shortToken));
        System.out.println("   长Token验证 (64字符): " + PacketValidator.isValidToken(longToken));
        System.out.println("   过长Token验证 (65字符): " + PacketValidator.isValidToken(tooLongToken));
        
        // 测试协议版本边界
        System.out.println("   协议版本0验证: " + PacketValidator.isSupportedProtocolVersion(0));
        System.out.println("   协议版本1验证: " + PacketValidator.isSupportedProtocolVersion(1));
        System.out.println("   协议版本3验证: " + PacketValidator.isSupportedProtocolVersion(3));
        System.out.println("   协议版本4验证: " + PacketValidator.isSupportedProtocolVersion(4));
        
        System.out.println("\n=== 示例结束 ===");
    }
}
