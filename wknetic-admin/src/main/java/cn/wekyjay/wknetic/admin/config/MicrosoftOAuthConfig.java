package cn.wekyjay.wknetic.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Microsoft OAuth 2.0 配置类
 * 使用设备流（Device Flow）模式进行认证
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wknetic.microsoft.oauth")
public class MicrosoftOAuthConfig {
    
    /**
     * Microsoft 官方设备流客户端ID（固定值）
     * 注意：这个客户端ID仅适用于Microsoft账户（消费者账户）
     */
    private String clientId = "00000000-402b-9631-c394-b33df003c47e";
    
    /**
     * 设备流不需要客户端密钥
     */
    private String clientSecret = "";
    
    /**
     * 设备流认证端点 - 获取设备码
     * 正确的Microsoft设备流端点
     */
    private String deviceCodeEndpoint = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    
    /**
     * 设备流令牌端点 - 轮询获取令牌
     */
    private String deviceTokenEndpoint = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    
    /**
     * 请求的权限范围
     * XboxLive.Signin - Xbox Live 登录（Minecraft相关）
     * offline_access - 获取刷新令牌
     */
    private String scope = "XboxLive.signin offline_access";
    
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
    
    /**
     * 设备流轮询间隔（毫秒）
     */
    private int pollingInterval = 5000;
    
    /**
     * 设备流轮询超时时间（毫秒）
     */
    private int pollingTimeout = 300000; // 5分钟
}
