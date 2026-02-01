package cn.wekyjay.wknetic.auth.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Security工具类
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
public class SecurityUtils {
    
    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        // 假设Principal是用户ID（Long类型）
        // 实际项目中需要根据JWT或UserDetails来获取
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
            // 如果是自定义UserDetails，从中提取userId
            // return ((CustomUserDetails) principal).getUserId();
            return 1L; // 临时返回，实际需要从认证信息中获取
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }
    
    /**
     * 判断是否为管理员
     *
     * @return true=是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * 判断是否为审核员
     *
     * @return true=是审核员
     */
    public static boolean isModerator() {
        return hasRole("MODERATOR") || hasRole("ADMIN");
    }
    
    /**
     * 判断是否拥有指定角色
     *
     * @param role 角色代码
     * @return true=拥有角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        
        String roleWithPrefix = "ROLE_" + role;
        return authorities.stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()) 
                        || roleWithPrefix.equals(authority.getAuthority()));
    }
}
