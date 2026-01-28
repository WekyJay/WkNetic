package cn.wekyjay.wknetic.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统配置实体类
 */
@Data
@TableName("sys_config")
public class SysConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 配置主键 */
    @TableId(type = IdType.AUTO)
    private Long configId;

    /** 配置键名（如：site.logo, site.name） */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置类型（string, number, boolean, json, image） */
    private String configType;

    /** 配置分组（system, site, email, upload等） */
    private String configGroup;

    /** 配置标签（用于前端显示） */
    private String configLabel;

    /** 配置描述 */
    private String configDesc;

    /** 是否系统内置（0否 1是，系统内置不可删除） */
    private Integer isSystem;

    /** 排序 */
    private Integer sortOrder;

    /** 状态（0停用 1启用） */
    private Integer status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
