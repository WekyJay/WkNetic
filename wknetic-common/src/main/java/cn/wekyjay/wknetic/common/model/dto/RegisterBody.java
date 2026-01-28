package cn.wekyjay.wknetic.common.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求体
 */
@Data
public class RegisterBody {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度在 3-20 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在 6-20 个字符")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String nickname;

    /** 验证码令牌 */
    private String captchaToken;

    /** 邮箱验证码（预留） */
    private String emailCode;
}
