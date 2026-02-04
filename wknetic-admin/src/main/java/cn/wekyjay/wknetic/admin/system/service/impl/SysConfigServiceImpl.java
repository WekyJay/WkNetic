package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.admin.system.domain.SysConfig;
import cn.wekyjay.wknetic.admin.system.mapper.SysConfigMapper;
import cn.wekyjay.wknetic.admin.system.service.ISysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置 Service 实现
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    @Override
    @Cacheable(value = "config", key = "#configKey", unless = "#result == null")
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey)
               .eq(SysConfig::getStatus, 1);
        SysConfig config = this.getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    @Override
    @Cacheable(value = "publicConfigs", unless = "#result == null || #result.isEmpty()")
    public Map<String, String> getPublicConfigs() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getIsPublic, 1)
               .eq(SysConfig::getStatus, 1);
        
        List<SysConfig> configs = this.list(wrapper);
        
        return configs.stream()
                .collect(Collectors.toMap(
                        SysConfig::getConfigKey,
                        config -> config.getConfigValue() != null ? config.getConfigValue() : "",
                        (v1, v2) -> v1
                ));
    }
}
