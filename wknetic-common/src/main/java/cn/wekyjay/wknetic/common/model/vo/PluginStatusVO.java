package cn.wekyjay.wknetic.common.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 插件状态VO
 */
@Data
public class PluginStatusVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 插件ID */
    private String pluginId;

    /** 插件名称 */
    private String pluginName;

    /** 插件版本 */
    private String pluginVersion;

    /** 是否启用 */
    private Boolean enabled;

    /** 已授予的权限列表 */
    private List<String> grantedPermissions;

    /** 安装时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date installedAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date updatedAt;
}
