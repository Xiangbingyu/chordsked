package com.chordsked.backend.security.jwt;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.security.account.service.MultiAccountUserDetailsService;
import com.chordsked.backend.utils.cookie.CookieUtils;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_TYPE = "userType";

    @Resource(name = "jwtTokenUtils")
    private JwtTokenUtils jwtTokenUtils;

    @Resource(name = "multiAccountUserDetailsService")
    private MultiAccountUserDetailsService multiAccountUserDetailsService;

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = CookieUtils.readCookieValue(request, CookieUtils.ACCESS_TOKEN_COOKIE_NAME);
        // 当前后台链路统一采用 HttpOnly Cookie 鉴权；仅在上下文未认证且请求携带 access_token 时执行认证。
        if (SecurityContextHolder.getContext().getAuthentication() == null
                && token != null
                && !token.isBlank()) {
            try {
                // 先解析 claims，再做 claims 校验，避免 isTokenValid 与 parseClaims 的重复解析
                Claims claims = jwtTokenUtils.parseClaims(token);
                // 业务 API 统一要求 access token；refresh token 仅用于续签接口
                if (jwtTokenUtils.isClaimsValid(claims, JwtTokenUtils.TOKEN_TYPE_ACCESS)) {
                    if (!securityCacheService.isTokenActive(token) || securityCacheService.isTokenRevoked(token)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    Long userId = claims.get(CLAIM_USER_ID, Long.class);
                    String userType = claims.get(CLAIM_USER_TYPE, String.class);
                    if (userId != null && userType != null && !userType.isBlank()) {
                        UserDetails userDetails = multiAccountUserDetailsService.loadUserDetailsByTokenContext(
                                userType,
                                userId
                        );
                        if (!userDetails.isEnabled()
                                || !userDetails.isAccountNonExpired()
                                || !userDetails.isCredentialsNonExpired()) {
                            logger.warn("User status invalid for request: {}", request.getRequestURI());
                            filterChain.doFilter(request, response);
                            return;
                        }
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
