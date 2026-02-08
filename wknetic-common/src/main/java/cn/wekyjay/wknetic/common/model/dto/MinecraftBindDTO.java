package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Minecraft账号绑定请求体
 */
@Data
public class MinecraftBindDTO {
    
    /** Minecraft UUID */
    @NotBlank(message = "Minecraft UUID不能为空")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", 
             message = "Minecraft UUID格式不正确")
    private String minecraftUuid;
    
    /** Minecraft用户名 */
    @NotBlank(message = "Minecraft用户名不能为空")
    private String minecraftUsername;
    
    /** 验证码（可选，用于二次验证） */
    private String verificationCode;
}