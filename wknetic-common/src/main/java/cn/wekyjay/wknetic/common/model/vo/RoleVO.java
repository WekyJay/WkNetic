package cn.wekyjay.wknetic.common.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class RoleVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long roleId;
    private String roleCode;
    private String roleName;
    private String roleDesc;
    private Integer sortOrder;
    private Boolean isDefault;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
