package cn.wekyjay.wknetic.common.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息")
public class ChatMessageVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "消息ID")
    private String id;
    
    @Schema(description = "服务器ID")
    private String serverName;
    
    @Schema(description = "频道")
    private String channel;
    
    @Schema(description = "世界ID")
    private String world;
    
    @Schema(description = "玩家信息")
    private PlayerInfo player;
    
    @Schema(description = "消息内容")
    private String content;
    
    @Schema(description = "消息来源: game-游戏内, web-网页")
    private String source;
    
    @Schema(description = "时间戳")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
    
    /**
     * 玩家信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "玩家信息")
    public static class PlayerInfo implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "玩家UUID")
        private String uuid;
        
        @Schema(description = "玩家名")
        private String username;
        
        @Schema(description = "头像URL")
        private String avatar;
    }
}
