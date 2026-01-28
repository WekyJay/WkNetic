package cn.wekyjay.wknetic.common.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求体
 */
@Data
public class LoginBody {

    @NotBlank(message = "{user.username.required}")
    private String username;

    @NotBlank(message = "{user.password.required}")
    private String password;

    /** 验证码令牌 */
    private String captchaToken;
    
    /** 记住我（30天），默认false（24小时） */
    private Boolean rememberMe = false;
}
