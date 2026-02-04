package cn.wekyjay.wknetic.admin.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表板快捷入口VO
 * 用于返回用户已设置的快捷入口列表
 * 
 * @author WkNetic
 * @since 2026-02-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardQuickActionVO {
    
    /**
     * 快捷入口ID
     */
    private Long actionId;
    
    /**
     * 快捷入口标识
     */
    private String actionKey;
    
    /**
     * 快捷入口名称
     */
    private String actionName;
    
    /**
     * 快捷入口URL路径
     */
    private String actionUrl;
    
    /**
     * 快捷入口图标
     */
    private String icon;
    
    /**
     * 显示顺序（1-4）
     */
    private Integer sortOrder;
}
