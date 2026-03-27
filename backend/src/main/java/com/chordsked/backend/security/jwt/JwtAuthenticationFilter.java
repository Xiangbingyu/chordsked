package com.chordsked.backend.security.jwt;

import com.chordsked.backend.security.account.service.MultiAccountUserDetailsService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component("jwtAuthenticationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Authorization: Bearer <token>
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_TYPE = "userType";

    @Autowired(required = false)
    private JwtTokenUtils jwtTokenUtils;

    @Autowired(required = false)
    private MultiAccountUserDetailsService multiAccountUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        // 仅在：依赖可用、上下文未认证、且请求带 Bearer Token 时执行鉴权
        if (jwtTokenUtils != null
                && multiAccountUserDetailsService != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && authorization != null
                && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length());
            try {
                // 第一阶段：校验签名、issuer、tokenType、userType 等基础约束
                if (jwtTokenUtils.isTokenValid(token, ACCESS_TOKEN_TYPE)) {
                    Claims claims = jwtTokenUtils.parseClaims(token);
                    Long userId = claims.get(CLAIM_USER_ID, Long.class);
                    String userType = claims.get(CLAIM_USER_TYPE, String.class);
                    if (userId != null && userType != null && !userType.isBlank()) {
                        // 第二阶段：按 userType 路由到对应 Provider 装载权限
                        UserDetails userDetails = multiAccountUserDetailsService.loadUserByTokenContext(
                                userType,
                                String.valueOf(userId)
                        );
                        // 第三阶段：写入 SecurityContext，供 @PreAuthorize 等后续判权使用
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (RuntimeException ignored) {
                // 鉴权失败时不抛出，让请求继续进入后续链路并由统一异常处理返回 401/403
            }
        }
        filterChain.doFilter(request, response);
    }
}
