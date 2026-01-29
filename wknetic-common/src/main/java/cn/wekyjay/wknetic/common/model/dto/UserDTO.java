package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * 创建/更新用户请求体
 */
@Data
public class UserDTO {
    /** 用户ID（更新时必填） */
    private Long userId;
    
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名必须为3-20位字母、数字或下划线")
    private String username;
    
    /** 密码（创建时必填，更新时选填） */
    private String password;
    
    /** 昵称 */
    private String nickname;
    
    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /** 手机号 */
    private String phone;
    
    /** 头像地址 */
    private String avatar;
    
    /** 状态 */
    private Integer status;
    
    /** 角色ID（推荐使用） */
    private Long roleId;
    
    /** 用户角色（兼容字段） */
    private String role;
    
    /** Minecraft UUID */
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$|^$", 
             message = "Minecraft UUID格式不正确")
    private String minecraftUuid;
    
    /** Minecraft用户名 */
    private String minecraftUsername;
}
