package cn.wekyjay.wknetic.common.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

/**
 * 更新插件状态请求DTO
 */
@Data
public class UpdatePluginStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 是否启用 */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
