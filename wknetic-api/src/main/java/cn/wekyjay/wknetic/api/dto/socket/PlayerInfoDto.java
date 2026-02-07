package cn.wekyjay.wknetic.api.dto.socket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 玩家信息DTO
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfoDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 玩家UUID
     */
    private String uuid;

    /**
     * 玩家名称
     */
    private String name;

    /**
     * 延迟（ping）
     */
    private Integer ping;

    /**
     * 所在世界
     */
    private String world;

    /**
     * 游戏模式
     */
    private String gameMode;
}
