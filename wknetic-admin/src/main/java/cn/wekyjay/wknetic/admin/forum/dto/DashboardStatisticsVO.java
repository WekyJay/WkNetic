package cn.wekyjay.wknetic.admin.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 仪表板统计卡片VO
 * 展示核心业务指标：用户、在线、发帖、待审核及其周环比
 * 
 * @author WkNetic
 * @since 2026-02-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatisticsVO {
    
    /**
     * 总用户数
     */
    private Long totalUserCount;
    
    /**
     * 总用户数周环比 (%)，负数表示下降
     */
    private BigDecimal totalUserChangeRate;
    
    /**
     * 当前在线用户数
     */
    private Long onlineUserCount;
    
    /**
     * 当前在线用户数周环比 (%)
     */
    private BigDecimal onlineUserChangeRate;
    
    /**
     * 总发帖数
     */
    private Long totalPostCount;
    
    /**
     * 总发帖数周环比 (%)
     */
    private BigDecimal totalPostChangeRate;
    
    /**
     * 待审核数（包括文章和评论举报）
     */
    private Long pendingAuditCount;
    
    /**
     * 待审核数周环比 (%)
     */
    private BigDecimal pendingAuditChangeRate;
}
