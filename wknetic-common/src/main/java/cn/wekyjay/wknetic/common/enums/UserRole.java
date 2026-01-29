package cn.wekyjay.wknetic.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRole {
    
    /** 管理员 - 拥有所有权限 */
    ADMIN("ADMIN", "管理员"),
    
    /** 版主 - 可以管理内容和用户 */
    MODERATOR("MODERATOR", "版主"),
    
    /** 普通用户 - 基础权限 */
    USER("USER", "普通用户"),
    
    /** VIP会员 - 额外功能权限 */
    VIP("VIP", "VIP会员"),
    
    /** 封禁用户 - 无法登录和操作 */
    BANNED("BANNED", "封禁");
    
    /** 角色代码 */
    private final String code;
    
    /** 角色名称 */
    private final String name;
    
    /**
     * 根据代码获取角色
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return USER; // 默认返回普通用户
    }
    
    /**
     * 验证角色代码是否有效
     */
    public static boolean isValid(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
