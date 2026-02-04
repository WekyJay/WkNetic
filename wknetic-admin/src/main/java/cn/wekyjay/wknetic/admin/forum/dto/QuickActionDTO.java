package cn.wekyjay.wknetic.admin.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 快捷入口DTO
 * 用户可自定义仪表板快捷操作入口，最多4个
 * 
 * @author WkNetic
 * @since 2026-02-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuickActionDTO {
    
    /**
     * 快捷入口ID（可选，更新时需要）
     */
    private Long actionId;
    
    /**
     * 快捷入口标识（如：post_list, audit_pending, user_manage, report_list）
     */
    @NotBlank(message = "快捷入口标识不能为空")
    private String actionKey;
    
    /**
     * 快捷入口名称（如：发帖管理、待审核、用户管理、举报处理）
     */
    @NotBlank(message = "快捷入口名称不能为空")
    private String actionName;
    
    /**
     * 快捷入口URL路径
     */
    @NotBlank(message = "快捷入口URL不能为空")
    private String actionUrl;
    
    /**
     * 快捷入口图标（Font Awesome或自定义）
     */
    private String icon;
    
    /**
     * 显示顺序（1-4）
     */
    @Min(value = 1, message = "排序顺序需要在1-4之间")
    private Integer sortOrder;
    
    /**
     * 是否启用
     */
    private Integer status;
}
