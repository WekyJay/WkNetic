package cn.wekyjay.wknetic.common.packet;

import cn.wekyjay.wknetic.api.enums.PacketType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public abstract class WkBasePacket implements Serializable {
    // 每一个数据包都应该明确自己的类型
    public abstract PacketType getType();

    // 可以在这里存放一些通用元数据，比如发送时间
    private long timestamp = System.currentTimeMillis();
}
