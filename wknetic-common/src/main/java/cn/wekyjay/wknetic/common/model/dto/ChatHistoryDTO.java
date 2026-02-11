package cn.wekyjay.wknetic.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;


/**
 * 聊天历史查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天历史查询")
public class ChatHistoryDTO {
    
    @Schema(description = "服务器名称")
    @NotBlank(message = "服务器名称不能为空")
    private String serverName;
    
    @Schema(description = "频道")
    private String channel;
    
    @Schema(description = "世界ID")
    private String world;
    
    @Schema(description = "获取数量", example = "100")
    private Integer limit;
}
