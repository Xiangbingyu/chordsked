package com.chordsked.backend.idempotent.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * 对写请求进行请求体缓存包装，供幂等组件读取 body 哈希。
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component("cachedBodyFilter")
public class CachedBodyFilter extends OncePerRequestFilter {
    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldWrap(request)) {
            filterChain.doFilter(new CachedBodyHttpServletRequest(request), response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldWrap(HttpServletRequest request) {
        if (!BODY_METHODS.contains(request.getMethod())) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        return normalizedContentType.contains("application/json")
                || normalizedContentType.contains("application/x-www-form-urlencoded")
                || normalizedContentType.contains("text/plain");
    }
}
