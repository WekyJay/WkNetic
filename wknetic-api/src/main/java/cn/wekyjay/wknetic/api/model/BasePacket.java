package cn.wekyjay.wknetic.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BasePacket {
        /**
     * Token（仅用于认证，不出现在日志中）
     */
    private String token;
    
}
