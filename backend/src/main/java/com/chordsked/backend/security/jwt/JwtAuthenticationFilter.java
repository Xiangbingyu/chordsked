package com.chordsked.backend.security.jwt;

import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("jwtAuthenticationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_TYPE = "userType";

    @Autowired(required = false)
    private JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (jwtTokenUtils != null && authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length());
            if (jwtTokenUtils.isTokenValid(token, ACCESS_TOKEN_TYPE)) {
                Claims claims = jwtTokenUtils.parseClaims(token);
                Long userId = claims.get(CLAIM_USER_ID, Long.class);
                String userType = claims.get(CLAIM_USER_TYPE, String.class);
                if (userId != null) {
                    // 基础框架阶段先按 userType 注入内置角色，后续可替换为数据库动态权限装载。
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            String.valueOf(userId), null, resolveAuthorities(userType)
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> resolveAuthorities(String userType) {
        if (userType == null || userType.isBlank()) {
            return List.of();
        }
        // 兼容 hasRole 与 hasAuthority 两种写法，后续接入 RBAC 可继续补充细粒度权限码。
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(2);
        authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + userType.toUpperCase()));
        authorities.add(new SimpleGrantedAuthority(userType.toUpperCase()));
        return authorities;
    }
}
