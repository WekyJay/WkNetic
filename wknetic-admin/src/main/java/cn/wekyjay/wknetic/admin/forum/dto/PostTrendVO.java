package cn.wekyjay.wknetic.admin.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 发帖趋势VO
 * 按日期统计发帖数量，用于绘制趋势图
 * 
 * @author WkNetic
 * @since 2026-02-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostTrendVO {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 该日期新发帖数
     */
    private Long postCount;
    
    /**
     * 该日期新评论数（可选）
     */
    private Long commentCount;
}
