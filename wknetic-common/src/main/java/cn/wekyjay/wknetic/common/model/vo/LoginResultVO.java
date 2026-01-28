package cn.wekyjay.wknetic.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** JWT Token */
    private String token;
    
    /** Token 过期时间（时间戳，毫秒） */
    private Long expiresAt;
}
