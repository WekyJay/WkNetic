package cn.wekyjay.wknetic.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统角色实体类
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long roleId;

    private String roleCode;
    private String roleName;
    private String roleDesc;
    private Integer sortOrder;
    private Boolean isDefault;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
