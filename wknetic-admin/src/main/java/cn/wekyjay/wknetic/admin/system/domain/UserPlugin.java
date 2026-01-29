package cn.wekyjay.wknetic.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户插件实体类
 */
@Data
@TableName("user_plugins")
public class UserPlugin implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 插件ID */
    private String pluginId;

    /** 插件名称 */
    private String pluginName;

    /** 插件版本 */
    private String pluginVersion;

    /** 是否启用（0禁用 1启用） */
    private Boolean enabled;

    /** 已授予的权限（JSON数组字符串） */
    private String grantedPermissions;

    /** 安装时间 */
    private Date installedAt;

    /** 更新时间 */
    private Date updatedAt;
}
