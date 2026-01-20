package cn.wekyjay.wknetic.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wknetic.jwt")
public class JwtProperties {
    /** 密钥 (必须复杂，否则 Hutool HS256 会报错) */
    private String secret = "WkNeticCoreSecretKeyForSpringSecurityVersion2026MakeItLong";
    
    /** Token 过期时间 (秒)，默认 30 天 (2592000) */
    private Long expiration = 2592000L;
    
    /** 续签阈值 (秒)，默认 30 分钟 (1800) */
    // 如果 Redis 中剩余时间小于这个值，且用户在操作，则自动续满 expiration
    private Long refreshTime = 1800L;
    
    /** 前端传递 Header 的名称 */
    private String header = "Authorization";
}