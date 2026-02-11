package cn.wekyjay.wknetic.api.model.packet;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServerLoginRespPacket extends ServerRespPacket {

    private String sessionId;

   
}
