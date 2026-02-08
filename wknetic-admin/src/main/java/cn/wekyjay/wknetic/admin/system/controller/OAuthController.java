package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.config.MicrosoftOAuthConfig;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.dto.MicrosoftOAuthCallbackDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import cn.wekyjay.wknetic.auth.model.LoginUser;
import cn.wekyjay.wknetic.common.utils.RedisUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * OAuth 2.0 认证控制器
 * 处理Microsoft OAuth授权和回调
 */
@Slf4j
@Tag(name = "OAuth 2.0", description = "OAuth 2.0 认证相关接口")
@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {

    @Autowired
    private MicrosoftOAuthConfig microsoftOAuthConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 获取Microsoft OAuth授权URL
     */
    @Operation(summary = "Get Microsoft OAuth Authorization URL", description = "获取Microsoft OAuth授权URL，用于前端重定向")
    @GetMapping("/microsoft/authorize")
    public Result<String> getMicrosoftAuthorizationUrl() {
        try {
            if (!microsoftOAuthConfig.isEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }

            // 生成随机state参数防止CSRF攻击
            String state = UUID.randomUUID().toString();
            
            // 将state存储到Redis，设置5分钟过期
            String redisKey = "oauth:state:" + state;
            redisUtils.set(redisKey, "pending", 5, TimeUnit.MINUTES); // 5分钟过期

            // 构建授权URL
            String authorizationUrl = UriComponentsBuilder.fromHttpUrl(microsoftOAuthConfig.getAuthorizationEndpoint())
                    .queryParam("client_id", microsoftOAuthConfig.getClientId())
                    .queryParam("response_type", "code")
                    .queryParam("redirect_uri", microsoftOAuthConfig.getRedirectUri())
                    .queryParam("scope", microsoftOAuthConfig.getScope())
                    .queryParam("state", state)
                    .queryParam("response_mode", "query")
                    .toUriString();

            return Result.success(authorizationUrl);
        } catch (Exception e) {
            log.error("获取Microsoft授权URL失败", e);
            return Result.error("获取Microsoft授权URL失败: " + e.getMessage());
        }
    }

    /**
     * Microsoft OAuth回调端点
     */
    @Operation(summary = "Microsoft OAuth Callback", description = "Microsoft OAuth回调端点，处理授权码交换")
    @GetMapping("/microsoft/callback")
    public Result<Map<String, Object>> microsoftCallback(MicrosoftOAuthCallbackDTO callbackDTO) {
        try {
            if (!microsoftOAuthConfig.isEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }

            // 检查错误
            if (callbackDTO.getError() != null) {
                log.error("Microsoft OAuth授权失败: {} - {}", callbackDTO.getError(), callbackDTO.getErrorDescription());
                return Result.error("Microsoft OAuth授权失败: " + callbackDTO.getError());
            }

            // 验证state参数
            String state = callbackDTO.getState();
            if (state == null || !redisUtils.hasKey("oauth:state:" + state)) {
                log.error("无效的state参数或state已过期");
                return Result.error("无效的state参数或state已过期");
            }

            // 删除已使用的state
            redisUtils.delete("oauth:state:" + state);

            String code = callbackDTO.getCode();
            if (code == null || code.isEmpty()) {
                return Result.error("缺少授权码");
            }

            // 获取当前登录用户ID
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }

            // 将授权码存储到Redis，关联当前用户
            String authCodeKey = "oauth:microsoft:code:" + userId;
            redisUtils.set(authCodeKey, code, 5, TimeUnit.MINUTES); // 5分钟过期

            // 返回前端需要的信息
            Map<String, Object> result = new HashMap<>();
            result.put("code", code);
            result.put("userId", userId);
            result.put("message", "授权码已接收，请在前端完成绑定流程");

            return Result.success(result);
        } catch (Exception e) {
            log.error("Microsoft OAuth回调处理失败", e);
            return Result.error("Microsoft OAuth回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 绑定Microsoft账户并获取Minecraft信息
     */
    @Operation(summary = "Bind Microsoft Account and Get Minecraft Info", description = "绑定Microsoft账户并获取Minecraft信息")
    @PostMapping("/microsoft/bind")
    public Result<Map<String, Object>> bindMicrosoftAccount() {
        try {
            if (!microsoftOAuthConfig.isEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }

            // 获取当前登录用户ID
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }

            // 从Redis获取授权码
            String authCodeKey = "oauth:microsoft:code:" + userId;
            String authCode = (String) redisUtils.get(authCodeKey);
            
            if (authCode == null || authCode.isEmpty()) {
                return Result.error("未找到有效的授权码，请重新授权");
            }

            // 删除已使用的授权码
            redisUtils.delete(authCodeKey);

            // 1. 使用授权码交换访问令牌
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("client_id", microsoftOAuthConfig.getClientId());
            tokenRequest.put("client_secret", microsoftOAuthConfig.getClientSecret());
            tokenRequest.put("code", authCode);
            tokenRequest.put("redirect_uri", microsoftOAuthConfig.getRedirectUri());
            tokenRequest.put("grant_type", "authorization_code");

            Map<String, Object> tokenResponse = restTemplate.postForObject(
                microsoftOAuthConfig.getTokenEndpoint(),
                tokenRequest,
                Map.class
            );

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                log.error("获取Microsoft访问令牌失败: {}", tokenResponse);
                return Result.error("获取Microsoft访问令牌失败");
            }

            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            
            // 2. 使用访问令牌获取Xbox Live令牌
            Map<String, Object> xboxAuthRequest = new HashMap<>();
            xboxAuthRequest.put("Properties", Map.of(
                "AuthMethod", "RPS",
                "SiteName", "user.auth.xboxlive.com",
                "RpsTicket", "d=" + accessToken
            ));
            xboxAuthRequest.put("RelyingParty", "http://auth.xboxlive.com");
            xboxAuthRequest.put("TokenType", "JWT");

            Map<String, Object> xboxResponse = restTemplate.postForObject(
                microsoftOAuthConfig.getXboxLiveAuthEndpoint(),
                xboxAuthRequest,
                Map.class
            );

            if (xboxResponse == null || !xboxResponse.containsKey("Token")) {
                log.error("获取Xbox Live令牌失败: {}", xboxResponse);
                return Result.error("获取Xbox Live令牌失败");
            }

            String xboxToken = (String) xboxResponse.get("Token");
            String userHash = null;
            
            if (xboxResponse.containsKey("DisplayClaims")) {
                Map<String, Object> displayClaims = (Map<String, Object>) xboxResponse.get("DisplayClaims");
                if (displayClaims.containsKey("xui")) {
                    List<Map<String, String>> xui = (List<Map<String, String>>) displayClaims.get("xui");
                    if (!xui.isEmpty()) {
                        userHash = xui.get(0).get("uhs");
                    }
                }
            }

            if (userHash == null) {
                log.error("无法获取用户哈希(UserHash)");
                return Result.error("无法获取用户身份信息");
            }

            // 3. 使用Xbox Live令牌获取XSTS令牌
            Map<String, Object> xstsRequest = new HashMap<>();
            xstsRequest.put("Properties", Map.of(
                "SandboxId", "RETAIL",
                "UserTokens", List.of(xboxToken)
            ));
            xstsRequest.put("RelyingParty", "rp://api.minecraftservices.com/");
            xstsRequest.put("TokenType", "JWT");

            Map<String, Object> xstsResponse = restTemplate.postForObject(
                microsoftOAuthConfig.getXstsAuthEndpoint(),
                xstsRequest,
                Map.class
            );

            if (xstsResponse == null || !xstsResponse.containsKey("Token")) {
                log.error("获取XSTS令牌失败: {}", xstsResponse);
                return Result.error("获取XSTS令牌失败");
            }

            String xstsToken = (String) xstsResponse.get("Token");

            // 4. 使用XSTS令牌获取Minecraft访问令牌
            Map<String, Object> minecraftAuthRequest = Map.of(
                "identityToken", String.format("XBL3.0 x=%s;%s", userHash, xstsToken)
            );

            Map<String, Object> minecraftTokenResponse = restTemplate.postForObject(
                microsoftOAuthConfig.getMinecraftAuthEndpoint(),
                minecraftAuthRequest,
                Map.class
            );

            if (minecraftTokenResponse == null || !minecraftTokenResponse.containsKey("access_token")) {
                log.error("获取Minecraft访问令牌失败: {}", minecraftTokenResponse);
                return Result.error("获取Minecraft访问令牌失败");
            }

            String minecraftAccessToken = (String) minecraftTokenResponse.get("access_token");
            String minecraftTokenType = (String) minecraftTokenResponse.get("token_type");
            int expiresIn = (int) minecraftTokenResponse.get("expires_in");

            // 5. 使用Minecraft访问令牌获取玩家档案
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(minecraftAccessToken);
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<Map> profileResponse = restTemplate.exchange(
                microsoftOAuthConfig.getMinecraftProfileEndpoint(),
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );

            if (profileResponse.getStatusCode() != org.springframework.http.HttpStatus.OK || 
                profileResponse.getBody() == null || 
                !profileResponse.getBody().containsKey("id")) {
                log.error("获取Minecraft玩家档案失败: {}", profileResponse.getBody());
                return Result.error("获取Minecraft玩家档案失败");
            }

            Map<String, Object> profile = profileResponse.getBody();
            String minecraftUuid = (String) profile.get("id");
            String minecraftUsername = (String) profile.get("name");

            // 6. 将绑定信息存储到数据库
            // 这里应该调用UserService来更新用户的Minecraft绑定信息
            // 暂时只返回获取到的信息
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Microsoft账户绑定成功");
            result.put("userId", userId);
            result.put("minecraftUuid", minecraftUuid);
            result.put("minecraftUsername", minecraftUsername);
            result.put("minecraftAccessToken", "已获取（不返回）");
            result.put("accessTokenType", minecraftTokenType);
            result.put("expiresIn", expiresIn);
            
            // 这里应该调用服务层保存绑定信息
            // 例如：userService.bindMinecraftAccount(userId, minecraftUuid, minecraftUsername);
            
            log.info("用户 {} 成功绑定Minecraft账户: {} (UUID: {})", 
                userId, minecraftUsername, minecraftUuid);

            return Result.success(result);
        } catch (Exception e) {
            log.error("绑定Microsoft账户失败", e);
            return Result.error("绑定Microsoft账户失败: " + e.getMessage());
        }
    }

    /**
     * 获取Microsoft OAuth配置状态
     */
    @Operation(summary = "Get Microsoft OAuth Config Status", description = "获取Microsoft OAuth配置状态")
    @GetMapping("/microsoft/config")
    public Result<Map<String, Object>> getMicrosoftConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("enabled", microsoftOAuthConfig.isEnabled());
            config.put("clientIdConfigured", !microsoftOAuthConfig.getClientId().isEmpty());
            config.put("clientSecretConfigured", !microsoftOAuthConfig.getClientSecret().isEmpty());
            config.put("redirectUri", microsoftOAuthConfig.getRedirectUri());
            config.put("scope", microsoftOAuthConfig.getScope());

            return Result.success(config);
        } catch (Exception e) {
            log.error("获取Microsoft配置失败", e);
            return Result.error("获取Microsoft配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return loginUser.getUserId();
        }
        return null;
    }

    /**
     * 提供RestTemplate bean
     */
    @org.springframework.context.annotation.Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}