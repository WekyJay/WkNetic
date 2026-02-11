package cn.wekyjay.wknetic.api.model.packet;

import cn.wekyjay.wknetic.api.enums.PacketType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class HeartbeatPacket extends BasePacket {

    @Override
    protected PacketType defineType() {
        return PacketType.HEARTBEAT;
    }

    
}
