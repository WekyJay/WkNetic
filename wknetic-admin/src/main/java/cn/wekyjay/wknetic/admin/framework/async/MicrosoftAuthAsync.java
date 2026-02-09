package cn.wekyjay.wknetic.admin.framework.async;


import java.util.concurrent.CompletableFuture;


import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import cn.wekyjay.wknetic.admin.system.service.DeviceFlowStateService;
import cn.wekyjay.wknetic.common.enums.DeviceFlowStatus;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.java.JavaAuthManager;
import net.raphimc.minecraftauth.java.model.MinecraftProfile;
import net.raphimc.minecraftauth.msa.model.MsaApplicationConfig;
import net.raphimc.minecraftauth.msa.model.MsaDeviceCode;
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService;
import java.util.function.Consumer;

@Service
@Slf4j
public class MicrosoftAuthAsync {
    /**
     * 异步方法：负责“脏活累活”（请求+轮询）
     * 必须标记 @Async，让它在独立的线程池中运行
     */
    @Async
    public void processDeviceFlowInternal(CompletableFuture<MsaDeviceCode> bridge, HttpClient client, MsaApplicationConfig msaAppConfig, DeviceFlowStateService deviceFlowStateService) {
        // 用于存储设备码的数组，以便在回调外访问
        final MsaDeviceCode[] deviceCodeHolder = new MsaDeviceCode[1];
        
        try {
            JavaAuthManager.Builder authManagerBuilder = JavaAuthManager.create(client)
                .msaApplicationConfig(msaAppConfig);

            log.info("异步线程开始：请求微软设备码...");

            // 核心逻辑：login 是阻塞的，会一直运行到用户扫码成功
            JavaAuthManager authManager = authManagerBuilder.login(DeviceCodeMsaAuthService::new, (Consumer<MsaDeviceCode>) deviceCode -> {
                // 【关键点】这里是回调！
                // 微软返回了设备码，立刻通过 bridge 通知主线程
                log.info("捕获到设备码: {}", deviceCode.getUserCode());

                // 存储设备码到数组，供后续使用
                deviceCodeHolder[0] = deviceCode;
                
                boolean notifySuccess = bridge.complete(deviceCode); 
                
                if (!notifySuccess) {
                    log.warn("主线程可能已经超时放弃，但异步线程继续执行业务逻辑...");
                }
                log.info("已通知主线程...");
                
                // 1. 先存 Redis (业务逻辑)
                deviceFlowStateService.createDeviceFlowState(
                    deviceCode.getUserCode(),
                    deviceCode.getDeviceCode(),
                    deviceCode.getVerificationUri(),
                    deviceCode.getVerificationUri() + "?user_code=" + deviceCode.getUserCode(),
                    null
                );
                
                log.info("设备码已保存到 Redis");
                
                // 3. 此时主线程拿到数据返回前端了，而本线程继续往下执行轮询...
            });

            // --- 下面的代码只有在用户【扫码完成后】才会执行 ---
            
            log.info("用户扫码完成，开始获取 Profile...");
            MinecraftProfile profile = authManager.getMinecraftProfile().getUpToDate();
            
            // 更新 Redis 状态为完成
            // 使用之前存储的设备码更新状态
            if (deviceCodeHolder[0] != null) {
                String deviceCode = deviceCodeHolder[0].getDeviceCode();
                log.info("更新设备流状态为完成，设备码: {}", deviceCode);
                
                // 更新Redis状态为完成，并设置Minecraft信息
                deviceFlowStateService.updateDeviceFlowStatus(
                    deviceCode,
                    DeviceFlowStatus.COMPLETED,
                    null, // 暂时不存储Microsoft访问令牌
                    profile.getId().toString(),
                    profile.getName()
                );
                
                log.info("设备流状态已更新为完成: {}", profile.getName());
            } else {
                log.warn("无法获取设备码，无法更新Redis状态");
            }

        } catch (Exception e) {
            log.error("异步认证过程异常", e);
            // 如果出错时还没给主线程返回，需要异常结束 bridge，否则主线程会死等直到超时
            if (!bridge.isDone()) {
                bridge.completeExceptionally(e);
            }
        }
    }
    
    
}
