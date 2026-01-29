package cn.wekyjay.wknetic.common.model.dto;

import lombok.Data;

/**
 * 用户查询参数
 */
@Data
public class UserQueryDTO {
    /** 页码 */
    private Integer page = 1;
    
    /** 每页大小 */
    private Integer size = 10;
    
    /** 关键词（用户名、邮箱、MC账号） */
    private String keyword;
    
    /** 状态筛选 */
    private Integer status;
    
    /** 角色筛选 */
    private String role;
}
