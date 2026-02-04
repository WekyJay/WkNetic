package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.admin.system.domain.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 系统配置 Service 接口
 */
public interface ISysConfigService extends IService<SysConfig> {
    
    /**
     * 根据配置键获取配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值，带默认值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 获取所有公开配置
     * 
     * @return 配置键值对
     */
    Map<String, String> getPublicConfigs();
}
