package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.admin.config.MicrosoftOAuthConfig;
import cn.wekyjay.wknetic.admin.system.service.ISysUserService;
import cn.wekyjay.wknetic.common.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import cn.wekyjay.wknetic.auth.model.LoginUser;
import cn.wekyjay.wknetic.common.utils.RedisUtils;
import co.elastic.clients.elasticsearch.security.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OAuth 2.0 认证控制器
 * 使用设备流（Device Flow）模式处理Microsoft OAuth认证
 */
@Slf4j
@Tag(name = "OAuth 2.0", description = "OAuth 2.0 认证相关接口（设备流模式）")
@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {

    @Autowired
    private MicrosoftOAuthConfig microsoftOAuthConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ISysUserService userService; // 假设有一个用户服务可以调用

    /**
     * 启动设备流认证流程 - 获取设备码
     */
    @Operation(summary = "Start Device Flow Authentication", description = "启动设备流认证流程，获取设备码和用户验证信息")
    @PostMapping("/microsoft/device-flow/start")
    public Result<Map<String, Object>> startDeviceFlow() {
        try {
            if (!microsoftOAuthConfig.isEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }

            // 获取当前登录用户ID
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }

            // 1. 请求设备码
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", microsoftOAuthConfig.getClientId());
            params.add("scope", microsoftOAuthConfig.getScope());
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            // 发送请求获取设备码
            Map<String, Object> response = restTemplate.postForObject(
                microsoftOAuthConfig.getDeviceCodeEndpoint(),
                request,
                Map.class
            );
            
            if (response == null || !response.containsKey("device_code")) {
                log.error("获取设备码失败: {}", response);
                return Result.error("获取设备码失败，请稍后重试");
            }
            
            String deviceCode = (String) response.get("device_code");
            String userCode = (String) response.get("user_code");
            String verificationUri = (String) response.get("verification_uri");
            Integer expiresIn = (Integer) response.get("expires_in");
            Integer interval = (Integer) response.get("interval");
            
            // 2. 将设备码和用户ID关联存储到Redis
            String deviceKey = "oauth:device:" + deviceCode;
            redisUtils.set(deviceKey, userId.toString(), expiresIn, TimeUnit.SECONDS);
            
            // 3. 返回用户验证信息
            Map<String, Object> result = new HashMap<>();
            result.put("device_code", deviceCode);
            result.put("user_code", userCode);
            result.put("verification_uri", verificationUri);
            result.put("expires_in", expiresIn);
            result.put("interval", interval != null ? interval : microsoftOAuthConfig.getPollingInterval() / 1000);
            result.put("message", "请在浏览器中访问 " + verificationUri + " 并输入代码: " + userCode);
            
            log.info("设备流认证已启动，用户ID: {}, 设备码: {}, 用户码: {}", userId, deviceCode, userCode);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("启动设备流认证失败", e);
            return Result.error("启动设备流认证失败: " + e.getMessage());
        }
    }

    /**
     * 轮询设备流认证状态 - 获取访问令牌
     */
    @Operation(summary = "Poll Device Flow Status", description = "轮询设备流认证状态，获取访问令牌")
    @PostMapping("/microsoft/device-flow/poll")
    public Result<Map<String, Object>> pollDeviceFlow(@RequestBody Map<String, String> requestBody) {
        try {
            if (!microsoftOAuthConfig.isEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }
            
            String deviceCode = requestBody.get("device_code");
            if (deviceCode == null || deviceCode.isEmpty()) {
                return Result.error("设备码不能为空");
            }
            
            // 1. 检查设备码是否有效（是否关联了用户ID）
            String deviceKey = "oauth:device:" + deviceCode;
            String userIdStr = redisUtils.get(deviceKey);
            if (userIdStr == null) {
                return Result.error("设备码已过期或无效");
            }
            
            // 2. 轮询获取访问令牌
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", microsoftOAuthConfig.getClientId());
            params.add("device_code", deviceCode);
            params.add("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            Map<String, Object> response = restTemplate.postForObject(
                microsoftOAuthConfig.getDeviceTokenEndpoint(),
                request,
                Map.class
            );
            
            if (response == null) {
                return Result.error("认证尚未完成，请稍后再试");
            }
            
            // 检查是否有错误
            if (response.containsKey("error")) {
                String error = (String) response.get("error");
                String errorDescription = response.containsKey("error_description") 
                    ? (String) response.get("error_description") 
                    : error;
                
                // 如果是授权等待中，返回特定状态码
                if ("authorization_pending".equals(error)) {
                    Map<String, Object> pendingResult = new HashMap<>();
                    pendingResult.put("status", "pending");
                    pendingResult.put("message", "等待用户授权");
                    return Result.success(pendingResult);
                }
                
                // 其他错误
                log.error("设备流认证错误: {}", errorDescription);
                return Result.error("认证失败: " + errorDescription);
            }
            
            // 3. 认证成功，获取访问令牌
            String accessToken = (String) response.get("access_token");
            String refreshToken = (String) response.get("refresh_token");
            Integer expiresIn = (Integer) response.get("expires_in");
            
            if (accessToken == null) {
                return Result.error("获取访问令牌失败");
            }
            
            // 4. 删除设备码，防止重复使用
            redisUtils.delete(deviceKey);
            
            // 5. 将访问令牌和用户ID关联存储到Redis（临时存储，用于后续绑定）
            String tokenKey = "oauth:token:" + accessToken;
            redisUtils.set(tokenKey, userIdStr, 300, TimeUnit.SECONDS); // 5分钟有效期
            
            // 6. 返回成功结果
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("access_token", accessToken);
            result.put("refresh_token", refreshToken);
            result.put("expires_in", expiresIn);
            result.put("message", "认证成功，正在绑定Minecraft账户...");
            
            log.info("设备流认证成功，用户ID: {}, 访问令牌已获取", userIdStr);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("轮询设备流认证状态失败", e);
            return Result.error("轮询设备流认证状态失败: " + e.getMessage());
        }
    }

    /**
     * 绑定Microsoft账户到Minecraft
     */
    @Operation(summary = "Bind Microsoft Account", description = "使用访问令牌绑定Microsoft账户到Minecraft")
    @PostMapping("/microsoft/bind")
    public Result<Map<String, Object>> bindMicrosoftAccount(@RequestBody Map<String, String> requestBody) {
        try {
            if (!microsoftOAuthConfig.isEnabled()) {
                return Result.error("Microsoft OAuth功能未启用");
            }
            
            String accessToken = requestBody.get("access_token");
            if (accessToken == null || accessToken.isEmpty()) {
                return Result.error("访问令牌不能为空");
            }
            
            // 1. 验证访问令牌是否有效（是否关联了用户ID）
            String tokenKey = "oauth:token:" + accessToken;
            // 获取并删除，防止重复使用
            String userIdStr = redisUtils.get(tokenKey);
            if (userIdStr != null) {
                redisUtils.delete(tokenKey);
            } else {
                return Result.error("访问令牌已过期或无效");
            }
            
            Long userId = Long.parseLong(userIdStr);
            
            // 2. 使用访问令牌获取Xbox Live令牌
            Map<String, Object> xboxToken = getXboxLiveToken(accessToken);
            if (xboxToken == null) {
                return Result.error("获取Xbox Live令牌失败");
            }
            
            // 3. 获取XSTS令牌
            Map<String, Object> xstsToken = getXSTSToken(xboxToken);
            if (xstsToken == null) {
                return Result.error("获取XSTS令牌失败");
            }
            
            // 4. 获取Minecraft访问令牌
            Map<String, Object> minecraftToken = getMinecraftToken(xstsToken);
            if (minecraftToken == null) {
                return Result.error("获取Minecraft令牌失败");
            }
            
            String minecraftAccessToken = (String) minecraftToken.get("access_token");
            
            // 5. 获取Minecraft档案信息
            Map<String, Object> minecraftProfile = getMinecraftProfile(minecraftAccessToken);
            if (minecraftProfile == null) {
                return Result.error("获取Minecraft档案信息失败");
            }
            
            String minecraftUuid = (String) minecraftProfile.get("id");
            String minecraftUsername = (String) minecraftProfile.get("name");
            
            if (minecraftUuid == null || minecraftUsername == null) {
                return Result.error("Minecraft档案信息不完整");
            }
            
            // 6. 保存Minecraft信息到用户账户
            boolean success = saveMinecraftInfo(userId, minecraftUuid, minecraftUsername);
            if (!success) {
                return Result.error("保存Minecraft信息失败");
            }
            
            // 7. 返回成功结果
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("minecraft_uuid", minecraftUuid);
            result.put("minecraft_username", minecraftUsername);
            result.put("message", "Minecraft账户绑定成功");
            
            log.info("Minecraft账户绑定成功，用户ID: {}, Minecraft用户名: {}, UUID: {}", 
                userId, minecraftUsername, minecraftUuid);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("绑定Microsoft账户失败", e);
            return Result.error("绑定Microsoft账户失败: " + e.getMessage());
        }
    }

    /**
     * 获取Microsoft OAuth配置信息
     */
    @Operation(summary = "Get Microsoft OAuth Config", description = "获取Microsoft OAuth配置信息")
    @GetMapping("/microsoft/config")
    public Result<Map<String, Object>> getMicrosoftConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("enabled", microsoftOAuthConfig.isEnabled());
            config.put("client_id", microsoftOAuthConfig.getClientId());
            config.put("scope", microsoftOAuthConfig.getScope());
            config.put("polling_interval", microsoftOAuthConfig.getPollingInterval());
            config.put("polling_timeout", microsoftOAuthConfig.getPollingTimeout());
            
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取Microsoft OAuth配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) authentication.getPrincipal();
                return loginUser.getUserId();
            }
            return null;
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return null;
        }
    }

    /**
     * 使用Microsoft访问令牌获取Xbox Live令牌
     */
    private Map<String, Object> getXboxLiveToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("Properties", Map.of(
                "AuthMethod", "RPS",
                "SiteName", "user.auth.xboxlive.com",
                "RpsTicket", "d=" + accessToken
            ));
            requestBody.put("RelyingParty", "http://auth.xboxlive.com");
            requestBody.put("TokenType", "JWT");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(
                microsoftOAuthConfig.getXboxLiveAuthEndpoint(),
                request,
                Map.class
            );
            
            return response;
        } catch (Exception e) {
            log.error("获取Xbox Live令牌失败", e);
            return null;
        }
    }

    /**
     * 使用Xbox Live令牌获取XSTS令牌
     */
    private Map<String, Object> getXSTSToken(Map<String, Object> xboxToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("Properties", Map.of(
                "SandboxId", "RETAIL",
                "UserTokens", List.of((String) xboxToken.get("Token"))
            ));
            requestBody.put("RelyingParty", "rp://api.minecraftservices.com/");
            requestBody.put("TokenType", "JWT");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(
                microsoftOAuthConfig.getXstsAuthEndpoint(),
                request,
                Map.class
            );
            
            return response;
        } catch (Exception e) {
            log.error("获取XSTS令牌失败", e);
            return null;
        }
    }

    /**
     * 使用XSTS令牌获取Minecraft访问令牌
     */
    private Map<String, Object> getMinecraftToken(Map<String, Object> xstsToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("identityToken", 
                "XBL3.0 x=" + xstsToken.get("DisplayClaims") + ";" + xstsToken.get("Token"));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(
                microsoftOAuthConfig.getMinecraftAuthEndpoint(),
                request,
                Map.class
            );
            
            return response;
        } catch (Exception e) {
            log.error("获取Minecraft令牌失败", e);
            return null;
        }
    }

    /**
     * 使用Minecraft访问令牌获取Minecraft档案信息
     */
    private Map<String, Object> getMinecraftProfile(String minecraftAccessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + minecraftAccessToken);
            headers.set("Accept", "application/json");
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            Map<String, Object> response = restTemplate.exchange(
                microsoftOAuthConfig.getMinecraftProfileEndpoint(),
                org.springframework.http.HttpMethod.GET,
                request,
                Map.class
            ).getBody();
            
            return response;
        } catch (Exception e) {
            log.error("获取Minecraft档案信息失败", e);
            return null;
        }
    }

    /**
     * 保存Minecraft信息到用户账户
     */
    private boolean saveMinecraftInfo(Long userId, String minecraftUuid, String minecraftUsername) {
        try {
            log.info("保存Minecraft信息 - 用户ID: {}, UUID: {}, 用户名: {}", 
                userId, minecraftUuid, minecraftUsername);
            
            // 调用用户服务的bindMinecraftAccount方法
            return userService.bindMinecraftAccount(userId, minecraftUuid, minecraftUsername);
        } catch (Exception e) {
            log.error("保存Minecraft信息失败", e);
            return false;
        }
    }
}
