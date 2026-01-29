package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.admin.system.domain.UserPlugin;
import cn.wekyjay.wknetic.common.model.dto.InstallPluginDTO;
import cn.wekyjay.wknetic.common.model.dto.UpdatePluginPermissionsDTO;
import cn.wekyjay.wknetic.common.model.vo.PluginStatusVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户插件 Service 接口
 */
public interface IUserPluginService extends IService<UserPlugin> {

    /**
     * 获取用户已安装的插件列表
     *
     * @param userId 用户ID
     * @return 插件状态列表
     */
    List<PluginStatusVO> getInstalledPlugins(Long userId);

    /**
     * 获取用户已启用的插件ID列表
     *
     * @param userId 用户ID
     * @return 插件ID列表
     */
    List<String> getEnabledPluginIds(Long userId);

    /**
     * 安装插件
     *
     * @param userId 用户ID
     * @param dto 安装请求DTO
     * @return 插件状态VO
     */
    PluginStatusVO installPlugin(Long userId, InstallPluginDTO dto);

    /**
     * 卸载插件
     *
     * @param userId 用户ID
     * @param pluginId 插件ID
     * @return 是否成功
     */
    boolean uninstallPlugin(Long userId, String pluginId);

    /**
     * 更新插件状态（启用/禁用）
     *
     * @param userId 用户ID
     * @param pluginId 插件ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean updatePluginStatus(Long userId, String pluginId, Boolean enabled);

    /**
     * 获取插件权限
     *
     * @param userId 用户ID
     * @param pluginId 插件ID
     * @return 权限列表
     */
    List<String> getPluginPermissions(Long userId, String pluginId);

    /**
     * 更新插件权限
     *
     * @param userId 用户ID
     * @param pluginId 插件ID
     * @param dto 更新权限DTO
     * @return 是否成功
     */
    boolean updatePluginPermissions(Long userId, String pluginId, UpdatePluginPermissionsDTO dto);
}
