package cn.wekyjay.wknetic.common.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户快捷入口实体
 * 用户可自定义仪表板快捷操作入口，最多4个
 * 
 * @author WkNetic
 * @since 2026-02-04
 */
@Data
@TableName("user_quick_action")
public class UserQuickAction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 快捷入口ID
     */
    @TableId(type = IdType.AUTO)
    private Long actionId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 快捷入口标识（唯一标识该快捷操作，如：post_list, audit_pending）
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
     * 图标（Font Awesome 或自定义图标类名）
     */
    private String icon;
    
    /**
     * 显示排序（1-4）
     */
    private Integer sortOrder;
    
    /**
     * 状态（0禁用 1启用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
