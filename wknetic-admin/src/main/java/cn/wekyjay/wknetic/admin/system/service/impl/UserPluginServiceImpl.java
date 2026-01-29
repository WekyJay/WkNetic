package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.admin.system.domain.UserPlugin;
import cn.wekyjay.wknetic.admin.system.mapper.UserPluginMapper;
import cn.wekyjay.wknetic.admin.system.service.IUserPluginService;
import cn.wekyjay.wknetic.common.model.dto.InstallPluginDTO;
import cn.wekyjay.wknetic.common.model.dto.UpdatePluginPermissionsDTO;
import cn.wekyjay.wknetic.common.model.vo.PluginStatusVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户插件 Service 实现类
 */
@Slf4j
@Service
public class UserPluginServiceImpl extends ServiceImpl<UserPluginMapper, UserPlugin> implements IUserPluginService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<PluginStatusVO> getInstalledPlugins(Long userId) {
        LambdaQueryWrapper<UserPlugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlugin::getUserId, userId)
               .orderByDesc(UserPlugin::getInstalledAt);
        
        List<UserPlugin> plugins = this.list(wrapper);
        
        return plugins.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<String> getEnabledPluginIds(Long userId) {
        LambdaQueryWrapper<UserPlugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlugin::getUserId, userId)
               .eq(UserPlugin::getEnabled, true)
               .select(UserPlugin::getPluginId);
        
        return this.list(wrapper).stream()
                .map(UserPlugin::getPluginId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PluginStatusVO installPlugin(Long userId, InstallPluginDTO dto) {
        // 检查是否已安装
        LambdaQueryWrapper<UserPlugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlugin::getUserId, userId)
               .eq(UserPlugin::getPluginId, dto.getPluginId());
        
        UserPlugin existingPlugin = this.getOne(wrapper);
        
        if (existingPlugin != null) {
            throw new RuntimeException("插件已安装，请先卸载后再安装");
        }
        
        // 创建新插件记录
        UserPlugin plugin = new UserPlugin();
        plugin.setUserId(userId);
        plugin.setPluginId(dto.getPluginId());
        plugin.setPluginName(dto.getPluginName());
        plugin.setPluginVersion(dto.getPluginVersion());
        plugin.setEnabled(true);
        plugin.setGrantedPermissions(convertPermissionsToJson(dto.getGrantedPermissions()));
        plugin.setInstalledAt(new Date());
        plugin.setUpdatedAt(new Date());
        
        this.save(plugin);
        
        return convertToVO(plugin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uninstallPlugin(Long userId, String pluginId) {
        LambdaQueryWrapper<UserPlugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlugin::getUserId, userId)
               .eq(UserPlugin::getPluginId, pluginId);
        
        int count = this.baseMapper.delete(wrapper);
        
        if (count == 0) {
            throw new RuntimeException("插件未安装或已被删除");
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePluginStatus(Long userId, String pluginId, Boolean enabled) {
        LambdaQueryWrapper<UserPlugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlugin::getUserId, userId)
               .eq(UserPlugin::getPluginId, pluginId);
        
        UserPlugin plugin = this.getOne(wrapper);
        
        if (plugin == null) {
            throw new RuntimeException("插件未安装");
        }
        
        plugin.setEnabled(enabled);
        plugin.setUpdatedAt(new Date());
        
        return this.updateById(plugin);
    }

    @Override
    public List<String> getPluginPermissions(Long userId, String pluginId) {
        LambdaQueryWrapper<UserPlugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlugin::getUserId, userId)
               .eq(UserPlugin::getPluginId, pluginId);
        
        UserPlugin plugin = this.getOne(wrapper);
        
        if (plugin == null) {
            throw new RuntimeException("插件未安装");
        }
        
        return convertJsonToPermissions(plugin.getGrantedPermissions());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePluginPermissions(Long userId, String pluginId, UpdatePluginPermissionsDTO dto) {
        LambdaQueryWrapper<UserPlugin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlugin::getUserId, userId)
               .eq(UserPlugin::getPluginId, pluginId);
        
        UserPlugin plugin = this.getOne(wrapper);
        
        if (plugin == null) {
            throw new RuntimeException("插件未安装");
        }
        
        plugin.setGrantedPermissions(convertPermissionsToJson(dto.getPermissions()));
        plugin.setUpdatedAt(new Date());
        
        return this.updateById(plugin);
    }

    /**
     * 将实体转换为VO
     */
    private PluginStatusVO convertToVO(UserPlugin plugin) {
        PluginStatusVO vo = new PluginStatusVO();
        BeanUtils.copyProperties(plugin, vo);
        vo.setGrantedPermissions(convertJsonToPermissions(plugin.getGrantedPermissions()));
        return vo;
    }

    /**
     * 将权限列表转换为JSON字符串
     */
    private String convertPermissionsToJson(List<String> permissions) {
        try {
            return objectMapper.writeValueAsString(permissions);
        } catch (JsonProcessingException e) {
            log.error("转换权限为JSON失败", e);
            return "[]";
        }
    }

    /**
     * 将JSON字符串转换为权限列表
     */
    private List<String> convertJsonToPermissions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("解析权限JSON失败: {}", json, e);
            return Collections.emptyList();
        }
    }
}
