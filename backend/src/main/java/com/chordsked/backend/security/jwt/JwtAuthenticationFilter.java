package com.chordsked.backend.security.jwt;

import com.chordsked.backend.security.account.service.MultiAccountUserDetailsService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    // Authorization: Bearer <token>
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
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
                // 先解析 claims，再做 claims 校验，避免 isTokenValid 与 parseClaims 的重复解析
                Claims claims = jwtTokenUtils.parseClaims(token);
                // 业务 API 统一要求 access token；refresh token 仅用于续签接口
                if (jwtTokenUtils.isClaimsValid(claims, JwtTokenUtils.TOKEN_TYPE_ACCESS)) {
                    Long userId = claims.get(CLAIM_USER_ID, Long.class);
                    String userType = claims.get(CLAIM_USER_TYPE, String.class);
                    if (userId != null && userType != null && !userType.isBlank()) {
                        // 根据 userType 路由到对应账号体系，加载权限集合
                        UserDetails userDetails = multiAccountUserDetailsService.loadUserByTokenContext(
                                userType,
                                userId
                        );
                        // 认证成功后写入上下文，供后续 requestMatchers/@PreAuthorize 判权
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    logger.warn("JWT claims validation failed for request: {}", request.getRequestURI());
                }
            } catch (ExpiredJwtException exception) {
                logger.debug("JWT token expired for request: {}", request.getRequestURI(), exception);
            } catch (MalformedJwtException exception) {
                logger.warn("Malformed JWT token in request: {}", request.getRequestURI(), exception);
            } catch (SignatureException exception) {
                logger.warn("JWT signature validation failed for request: {}", request.getRequestURI(), exception);
            } catch (RuntimeException exception) {
                logger.error("JWT authentication failed for request: {}", request.getRequestURI(), exception);
            }
        }
        filterChain.doFilter(request, response);
    }
}
