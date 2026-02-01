package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建举报DTO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
public class CreateReportDTO {
    
    /**
     * 举报对象类型：POST/COMMENT
     */
    @NotBlank(message = "举报对象类型不能为空")
    private String targetType;
    
    /**
     * 举报对象ID
     */
    @NotNull(message = "举报对象ID不能为空")
    private Long targetId;
    
    /**
     * 举报原因
     */
    @NotBlank(message = "举报原因不能为空")
    private String reason;
}
