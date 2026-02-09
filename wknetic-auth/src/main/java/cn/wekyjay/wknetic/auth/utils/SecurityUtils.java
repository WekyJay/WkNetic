package cn.wekyjay.wknetic.auth.utils;

import cn.wekyjay.wknetic.auth.model.LoginUser;
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
        
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) principal;
                return loginUser.getUserId();
            } else if (principal instanceof Long) {
                return (Long) principal;
            } else if (principal instanceof String) {
                // 处理字符串类型的principal（如用户名）
                // 这里可以尝试从数据库查询用户ID
                // 暂时返回null，由调用方处理
                return null;
            }
            return null;
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
