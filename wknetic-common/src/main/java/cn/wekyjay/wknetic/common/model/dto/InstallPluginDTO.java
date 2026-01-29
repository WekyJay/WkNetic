package cn.wekyjay.wknetic.common.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 安装插件请求DTO
 */
@Data
public class InstallPluginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 插件ID */
    @NotBlank(message = "插件ID不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "插件ID格式不正确")
    private String pluginId;

    /** 插件名称 */
    @NotBlank(message = "插件名称不能为空")
    private String pluginName;

    /** 插件版本 */
    @NotBlank(message = "插件版本不能为空")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "插件版本格式不正确（需要x.y.z格式）")
    private String pluginVersion;

    /** 授予的权限列表 */
    @NotNull(message = "权限列表不能为空")
    private List<String> grantedPermissions;
}
