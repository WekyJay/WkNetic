package cn.wekyjay.wknetic.api.model.packet;

import cn.wekyjay.wknetic.api.enums.PacketType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 基础Packet类，所有协议Packet的基类
 * 包含通用的验证注解，确保数据完整性
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BasePacket {
    /**
     * Packet 类型 - 不能为空
     */
    @NotNull(message = "Packet类型不能为空")
    private PacketType type;
    
    /**
     * Token（仅用于认证，不出现在日志中）
     * 长度必须在32-64字符之间，不能为空
     */
    @NotBlank(message = "Token不能为空")
    @Size(min = 32, max = 64, message = "Token长度必须在32-64字符之间")
    private String token;
    
    /**
     * 协议版本 - 用于向后兼容
     * 默认值为1，必须大于0
     */
    @Min(value = 1, message = "协议版本必须大于0")
    private int protocolVersion = 1;
    
    /**
     * 时间戳 - 用于记录Packet创建时间
     */
    private long timestamp = System.currentTimeMillis();
    
    /**
     * 验证Packet数据是否有效（基础验证）
     * @return 如果数据有效返回true，否则返回false
     */
    public boolean isValid() {
        return type != null && 
               token != null && 
               token.length() >= 32 && 
               token.length() <= 64 && 
               protocolVersion >= 1;
    }
}
