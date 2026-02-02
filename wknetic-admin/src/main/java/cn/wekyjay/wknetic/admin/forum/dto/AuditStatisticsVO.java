package cn.wekyjay.wknetic.admin.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 审核统计VO
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditStatisticsVO {
    
    /**
     * 待审核数量
     */
    private Long pendingCount;
    
    /**
     * 今日审核通过数
     */
    private Long todayApprovedCount;
    
    /**
     * 今日审核拒绝数
     */
    private Long todayRejectedCount;
    
    /**
     * 本周审核通过数
     */
    private Long weekApprovedCount;
    
    /**
     * 本周审核拒绝数
     */
    private Long weekRejectedCount;
    
    /**
     * 通过率 (%)
     */
    private BigDecimal approvalRate;
    
    /**
     * 平均审核时间（分钟）
     */
    private Long averageAuditTime;
    
    /**
     * 总审核数
     */
    private Long totalAuditCount;
}
