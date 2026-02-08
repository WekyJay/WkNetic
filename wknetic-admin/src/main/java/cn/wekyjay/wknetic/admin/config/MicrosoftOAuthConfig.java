package cn.wekyjay.wknetic.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Microsoft OAuth 2.0 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wknetic.microsoft.oauth")
public class MicrosoftOAuthConfig {
    
    /**
     * Microsoft Azure 应用注册的客户端ID
     */
    private String clientId = "";
    
    /**
     * Microsoft Azure 应用注册的客户端密钥
     */
    private String clientSecret = "";
    
    /**
     * 重定向URI，需要与Azure门户中注册的重定向URI匹配
     */
    private String redirectUri = "http://localhost:8080/api/v1/oauth/microsoft/callback";
    
    /**
     * Microsoft OAuth 2.0 授权端点
     */
    private String authorizationEndpoint = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
    
    /**
     * Microsoft OAuth 2.0 令牌端点
     */
    private String tokenEndpoint = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    
    /**
     * Microsoft Graph API 用户信息端点
     */
    private String userInfoEndpoint = "https://graph.microsoft.com/v1.0/me";
    
    /**
     * 请求的权限范围
     * offline_access - 获取刷新令牌
     * XboxLive.signin - Xbox Live 登录（Minecraft相关）
     * XboxLive.offline_access - Xbox Live 离线访问
     */
    private String scope = "XboxLive.signin XboxLive.offline_access offline_access";
    
    /**
     * Minecraft服务API端点
     */
    private String minecraftServicesEndpoint = "https://api.minecraftservices.com";
    
    /**
     * Xbox Live身份验证端点
     */
    private String xboxLiveAuthEndpoint = "https://user.auth.xboxlive.com/user/authenticate";
    
    /**
     * Xbox Live XSTS端点
     */
    private String xstsAuthEndpoint = "https://xsts.auth.xboxlive.com/xsts/authorize";
    
    /**
     * Minecraft服务登录端点
     */
    private String minecraftAuthEndpoint = "https://api.minecraftservices.com/authentication/login_with_xbox";
    
    /**
     * Minecraft档案端点
     */
    private String minecraftProfileEndpoint = "https://api.minecraftservices.com/minecraft/profile";
    
    /**
     * 启用/禁用Microsoft OAuth功能
     */
    private boolean enabled = false;
}