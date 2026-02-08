package cn.wekyjay.wknetic.api.model.packet;

import cn.wekyjay.wknetic.api.enums.PacketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PlayerChatPacket extends ServerPacket {
    @Builder.Default
    private PacketType type = PacketType.CHAT_MSG;
    private String player;
    private String msg;
    private String uuid;
    private String world;
    private long time;
}
