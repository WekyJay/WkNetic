package cn.wekyjay.wknetic.common.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * JWT 密钥安全初始化器
 * 用于在用户未配置密钥时，自动生成并持久化一个随机密钥
 */
@Slf4j
@Component
public class JwtSecretInitializer implements SmartInitializingSingleton {

    @Autowired
    JwtProperties jwtProperties;

    // 必须与 application.yml 中的默认值保持完全一致
    private static final String DEFAULT_DEV_KEY = "WkNeticCoreSecretKeyForSpringSecurityVersion2026MakeItLong";
    
    // 密钥持久化文件路径 (建议放在 data 目录下，方便 Docker 挂载)
    private static final String SECRET_FILE_PATH = "data/.jwt_secret";

    public JwtSecretInitializer(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void afterSingletonsInstantiated() {
        String currentSecret = jwtProperties.getSecret();

        // 1. 如果用户已经在 yml 或环境变量中配置了自定义密钥，则跳过
        if (!DEFAULT_DEV_KEY.equals(currentSecret)) {
            log.info("检测到自定义 JWT 密钥，安全检查通过。");
            return;
        }

        // 2. 如果是默认密钥，开始执行“持久化文件”逻辑
        File secretFile = new File(SECRET_FILE_PATH);

        if (secretFile.exists()) {
            // A. 文件存在：说明不是第一次启动，直接读取旧密钥
            String fileSecret = FileUtil.readString(secretFile, CharsetUtil.CHARSET_UTF_8).trim();
            if (!fileSecret.isEmpty()) {
                jwtProperties.setSecret(fileSecret);
                log.warn("=====================================================");
                log.warn("【WkNetic】正在使用自动生成的本地密钥 (来源于 {})", SECRET_FILE_PATH);
                log.warn("为了更高的安全性，建议您在环境变量中设置 WKNETIC_JWT_SECRET");
                log.warn("=====================================================");
                return;
            }
        }

        // B. 文件不存在：说明是首次启动，生成新密钥并保存
        initializeRandomSecret(secretFile);
    }

    private void initializeRandomSecret(File secretFile) {
        // 生成一个强随机密钥 (Hutool 的 HmacSHA256 需要较长的 Key，这里生成 64 位随机字符)
        String newRandomSecret = IdUtil.fastSimpleUUID() + IdUtil.fastSimpleUUID();
        
        try {
            // 写入文件 (自动创建父目录)
            FileUtil.writeString(newRandomSecret, secretFile, CharsetUtil.UTF_8);
            
            // 更新内存中的配置
            jwtProperties.setSecret(newRandomSecret);

            log.warn("\n" +
                    "#############################################################\n" +
                    "#  警告：检测到默认密钥，已自动生成随机安全密钥！               #\n" +
                    "#-----------------------------------------------------------#\n" +
                    "#  新密钥已保存至: " + secretFile.getAbsolutePath() + "\n" +
                    "#  应用重启后密钥将保持不变 (只要 data 目录不被删除)。         #\n" +
                    "#  如果您删除了该文件，所有用户的登录状态将失效。             #\n" +
                    "#############################################################");
                    
        } catch (Exception e) {
            log.error("无法写入密钥文件，请检查磁盘权限！", e);
            // 降级策略：虽然无法保存，但内存里也要改掉，防止裸奔
            jwtProperties.setSecret(newRandomSecret); 
        }
    }
}