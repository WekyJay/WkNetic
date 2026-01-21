package cn.wekyjay.wknetic.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginBody {

    @NotBlank(message = "{user.username.required}")
    private String username;

    @NotBlank(message = "{user.password.required}")
    private String password;
}