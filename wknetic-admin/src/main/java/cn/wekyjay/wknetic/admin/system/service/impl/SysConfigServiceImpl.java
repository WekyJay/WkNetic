package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.admin.system.domain.SysConfig;
import cn.wekyjay.wknetic.admin.system.mapper.SysConfigMapper;
import cn.wekyjay.wknetic.admin.system.service.ISysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
}
