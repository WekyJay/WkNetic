package cn.wekyjay.wknetic.api.model.packet;

import cn.wekyjay.wknetic.api.enums.PacketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServerRespPacket extends BasePacket {
    private boolean success;
    private String message;

    @Override
    protected PacketType defineType() {
        return PacketType.SERVER_RESP;
    }
    
}
