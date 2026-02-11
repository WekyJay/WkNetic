package cn.wekyjay.wknetic.api.example;

import cn.wekyjay.wknetic.api.enums.PacketType;
import cn.wekyjay.wknetic.api.model.packet.ServerLoginPacket;
import cn.wekyjay.wknetic.api.utils.PacketUtils;
import cn.wekyjay.wknetic.api.utils.PacketValidator;

import java.io.IOException;


/**
 * Gson序列化示例
 * 演示使用Gson进行Packet序列化和反序列化
 */
public class GsonExample {
    
    public static void main(String[] args) {
        System.out.println("=== Gson序列化示例 ===\n");
        
        try {
            // 1. 创建ServerLoginPacket
            ServerLoginPacket packet = new ServerLoginPacket();
            packet.setToken("test_token_1234567890_abcdefghijklmnopqrstuvwxyz");
            packet.setProtocolVersion(1);
            packet.setServerIp("192.168.1.100");
            
            System.out.println("1. 原始Packet:");
            System.out.println("   Type: " + packet.getType());
            System.out.println("   Token: " + packet.getToken());
            System.out.println("   ProtocolVersion: " + packet.getProtocolVersion());
            System.out.println("   ServerIp: " + packet.getServerIp());
            
            // 2. 验证Packet
            boolean isValid = PacketValidator.isValid(packet);
            System.out.println("\n2. Packet验证结果: " + (isValid ? "通过" : "失败"));
            
            // 打印详细的验证信息
            PacketValidator.ValidationResult validationResult = PacketValidator.validate(packet);
            System.out.println("   详细验证结果: " + validationResult.getFormattedErrorMessage());
            
            if (!isValid) {
                System.out.println("   Packet验证失败，无法继续测试");
                return;
            }
            
            // 3. 使用Gson序列化为JSON
            System.out.println("\n3. 使用Gson序列化为JSON:");
            String json = PacketUtils.toJson(packet);
            System.out.println("   JSON字符串:");
            System.out.println("   " + json);
            
            // 4. 使用Gson反序列化
            System.out.println("\n4. 使用Gson反序列化:");
            ServerLoginPacket deserializedPacket = PacketUtils.fromJson(json, ServerLoginPacket.class);
            System.out.println("   反序列化后的Packet:");
            System.out.println("   Type: " + deserializedPacket.getType());
            System.out.println("   Token: " + deserializedPacket.getToken());
            System.out.println("   ProtocolVersion: " + deserializedPacket.getProtocolVersion());
            System.out.println("   ServerIp: " + deserializedPacket.getServerIp());
            
            // 5. 验证反序列化后的Packet
            boolean deserializedIsValid = PacketValidator.isValid(deserializedPacket);
            System.out.println("\n5. 反序列化Packet验证结果: " + (deserializedIsValid ? "通过" : "失败"));
            
            // 6. 测试字节数组序列化
            System.out.println("\n6. 测试字节数组序列化:");
            byte[] bytes = PacketUtils.toBytes(packet);
            System.out.println("   字节数组长度: " + bytes.length + " bytes");
            
            // 7. 从字节数组反序列化
            ServerLoginPacket fromBytesPacket = PacketUtils.fromBytes(bytes, ServerLoginPacket.class);
            System.out.println("   从字节数组反序列化的Packet验证: " + 
                (PacketValidator.isValid(fromBytesPacket) ? "通过" : "失败"));
            
            // 8. 测试安全方法
            System.out.println("\n7. 测试安全方法:");
            String safeJson = PacketUtils.toJsonSafe(packet);
            System.out.println("   toJsonSafe结果: " + (safeJson != null ? "成功" : "失败"));
            
            ServerLoginPacket safeDeserialized = PacketUtils.fromJsonSafe(safeJson, ServerLoginPacket.class);
            System.out.println("   fromJsonSafe结果: " + (safeDeserialized != null ? "成功" : "失败"));
            
            // 9. 测试深拷贝
            System.out.println("\n8. 测试深拷贝:");
            ServerLoginPacket deepCopy = PacketUtils.deepCopy(packet, ServerLoginPacket.class);
            System.out.println("   深拷贝验证: " + (PacketValidator.isValid(deepCopy) ? "通过" : "失败"));
            System.out.println("   原始Packet和深拷贝是否相等: " + packet.equals(deepCopy));
            
            // 10. 测试美化JSON输出
            System.out.println("\n9. 测试美化JSON输出:");
            String prettyJson = PacketUtils.toPrettyJson(packet);
            System.out.println("   美化JSON:");
            System.out.println(prettyJson);
            
            // 11. 测试无效JSON处理
            System.out.println("\n10. 测试无效JSON处理:");
            String invalidJson = "{invalid json}";
            ServerLoginPacket invalidPacket = PacketUtils.fromJsonSafe(invalidJson, ServerLoginPacket.class);
            System.out.println("   无效JSON反序列化结果: " + (invalidPacket == null ? "正确处理（返回null）" : "错误"));
            
        } catch (IOException e) {
            System.err.println("序列化/反序列化失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== 示例结束 ===");
    }
}
