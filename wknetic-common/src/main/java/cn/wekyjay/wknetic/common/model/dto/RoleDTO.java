package cn.wekyjay.wknetic.common.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

@Data
public class RoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long roleId;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String roleDesc;

    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    private Boolean isDefault;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
