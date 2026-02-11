package cn.wekyjay.wknetic.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * 发送聊天消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送聊天消息")
public class SendChatMessageDTO {
    
    @Schema(description = "服务器名称")
    @NotBlank(message = "服务器名称不能为空")
    private String serverName;
    
    @Schema(description = "频道")
    @NotBlank(message = "频道不能为空")
    private String channel;
    
    @Schema(description = "世界ID")
    private String world;
    
    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不能超过500个字符")
    private String content;
}
