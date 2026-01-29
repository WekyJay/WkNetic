package cn.wekyjay.wknetic.auth.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 登录用户身份权限对象
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private String role; // 用户角色：ADMIN, MODERATOR, USER, VIP, BANNED
    
    // 可以在这里加 avatar 等其他业务字段

    public LoginUser(Long userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    public LoginUser(Long userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色转换为 Spring Security 的 GrantedAuthority
        // Spring Security 的 hasRole() 会自动添加 "ROLE_" 前缀
        if (role != null && !role.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}