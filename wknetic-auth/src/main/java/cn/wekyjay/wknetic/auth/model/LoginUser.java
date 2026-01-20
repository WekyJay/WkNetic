package cn.wekyjay.wknetic.auth.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 登录用户身份权限对象
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    
    // 可以在这里加 role, avatar 等其他业务字段

    public LoginUser(Long userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 暂时返回空权限，后续从数据库查角色
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