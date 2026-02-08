package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Microsoft OAuth 2.0 回调参数DTO
 */
@Data
@NoArgsConstructor
public class MicrosoftOAuthCallbackDTO {
    
    /**
     * OAuth 2.0 授权码
     */
    private String code;
    
    /**
     * 状态参数，用于防止CSRF攻击
     */
    private String state;
    
    /**
     * 错误码（当授权失败时）
     */
    private String error;
    
    /**
     * 错误描述（当授权失败时）
     */
    private String errorDescription;
}