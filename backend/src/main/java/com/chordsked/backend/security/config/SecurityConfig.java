package com.chordsked.backend.security.config;

import com.chordsked.backend.exception.SecurityExceptionHandler;
import com.chordsked.backend.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean("securityFilterChain")
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            SecurityExceptionHandler securityExceptionHandler
    ) throws Exception {
        httpSecurity
                // 基础 JWT 框架阶段采用无状态鉴权，后续可按模块接入 csrf token 方案。
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 预留认证端点，后续接入登录/续签实现时无需再改安全主干。
                        .requestMatchers(
                                "/admin/api/v1/login",
                                "/teachers/api/v1/login",
                                "/students/api/v1/login",
                                "/admin/api/v1/refresh",
                                "/teachers/api/v1/refresh",
                                "/students/api/v1/refresh",
                                "/admin/api/v1/logout",
                                "/teachers/api/v1/logout",
                                "/students/api/v1/logout"
                        ).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/admin/api/v1", "/admin/api/v1/**").hasAuthority("admin:role")
                        .requestMatchers("/teachers/api/v1", "/teachers/api/v1/**").hasAuthority("teacher:role")
                        .requestMatchers("/students/api/v1", "/students/api/v1/**").hasAuthority("student:role")
                        .requestMatchers("/api/v1/common/**").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 统一输出 401/403 JSON，避免前端收到默认 HTML 错误页。
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(securityExceptionHandler)
                        .accessDeniedHandler(securityExceptionHandler)
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }
}
