package cn.wekyjay.wknetic.admin.security;

import cn.wekyjay.wknetic.auth.security.JwtAuthenticationTokenFilter;
import cn.wekyjay.wknetic.auth.security.RestAccessDeniedHandler;
import cn.wekyjay.wknetic.auth.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;

/**
 * @Description:  
 * @Title: SecurityConfig配置类
 * @author WekyJay
 * @Github: <a href="https://github.com/WekyJay">https://github.com/WekyJay</a>
 * @Date: 2026/1/20 13:23
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Resource
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    @Resource
    private RestAccessDeniedHandler restAccessDeniedHandler;

    /**
     * Spring Security 6.0+ 核心配置链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用 CSRF (因为我们是前后端分离 + JWT，不需要 Session)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. 启用 CORS (允许 Vue 前端跨域)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. 异常处理 (指定我们自定义的 401/403 处理器)
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler)
            )
            
            // 4. 会话管理 (设置为无状态，不创建 Session)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 5. 请求拦截规则
            .authorizeHttpRequests(registry -> registry
                // 允许匿名访问的接口
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 跨域预检
                .requestMatchers("/api/auth/**").permitAll()            // 登录注册接口
                .requestMatchers("/api/open/**").permitAll()            // 开放签名接口
                .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**").permitAll() // Swagger
                // 其他所有请求必须认证
                .anyRequest().authenticated()
            )
            
            // 6. 将 JWT 过滤器添加到 UsernamePasswordAuthenticationFilter 之前
            .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码加密器 (BCrypt)
     * 数据库里存的密码不能是明文，必须是用这个加密过的
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器 (登录接口需要用到它来验证账号密码)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 跨域配置 (CORS)
     * 允许前端 Vue 项目访问后端
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*")); // 允许所有来源 (开发环境)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}