package cn.wekyjay.wknetic.admin.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 最近活动日志VO
 * 展示系统操作日志和重要事件
 * 
 * @author WkNetic
 * @since 2026-02-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentActivityVO {
    
    /**
     * 操作日志ID
     */
    private Long logId;
    
    /**
     * 操作标题
     */
    private String title;
    
    /**
     * 业务类型（新增=1, 修改=2, 删除=3, 审批=4, 等等）
     */
    private Integer businessType;
    
    /**
     * 业务类型标签
     */
    private String businessTypeLabel;
    
    /**
     * 操作用户名
     */
    private String operName;
    
    /**
     * 操作时间
     */
    private LocalDateTime operTime;
    
    /**
     * 操作状态（0失败 1成功）
     */
    private Integer status;
    
    /**
     * 状态标签
     */
    private String statusLabel;
    
    /**
     * 操作IP地址
     */
    private String operIp;
    
    /**
     * 错误信息（仅当status=0时有值）
     */
    private String errorMsg;
}
