package cn.wekyjay.wknetic.common.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 更新插件权限请求DTO
 */
@Data
public class UpdatePluginPermissionsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 权限列表 */
    @NotNull(message = "权限列表不能为空")
    private List<String> permissions;
}
