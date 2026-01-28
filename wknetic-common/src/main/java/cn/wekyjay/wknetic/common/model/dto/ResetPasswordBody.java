package cn.wekyjay.wknetic.common.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求体
 */
@Data
public class ResetPasswordBody {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在 6-20 个字符")
    private String newPassword;

    /** 验证码令牌 */
    private String captchaToken;

    /** 邮箱验证码（预留，未来可用于验证用户身份） */
    private String emailCode;
}
