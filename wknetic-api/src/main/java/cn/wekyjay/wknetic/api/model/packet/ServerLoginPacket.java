package cn.wekyjay.wknetic.api.model.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

import cn.wekyjay.wknetic.api.enums.PacketType;

/**
 * 游戏服务器登录数据包
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServerLoginPacket extends ServerPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 服务器IP地址
     */
    private String serverIp;
    
    @Override
    protected PacketType defineType() {
        return PacketType.SERVER_LOGIN;
    }
}