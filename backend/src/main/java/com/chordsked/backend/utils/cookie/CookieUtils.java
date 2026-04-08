package com.chordsked.backend.utils.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.util.Objects;

public final class CookieUtils {
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/";
    private static final String SAME_SITE_POLICY = "Lax";

    private CookieUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void writeCookie(
            HttpServletResponse response,
            String cookieName,
            String cookieValue,
            long maxAgeSeconds,
            boolean secure
    ) {
        Objects.requireNonNull(response, "response must not be null");
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                buildCookie(cookieName, cookieValue, maxAgeSeconds, secure).toString()
        );
    }

    public static void clearCookie(HttpServletResponse response, String cookieName, boolean secure) {
        Objects.requireNonNull(response, "response must not be null");
        response.addHeader(HttpHeaders.SET_COOKIE, buildClearedCookie(cookieName, secure).toString());
    }

    public static String readCookieValue(HttpServletRequest request, String cookieName) {
        Objects.requireNonNull(request, "request must not be null");
        String normalizedCookieName = Objects.requireNonNull(cookieName, "cookieName must not be null").trim();
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie != null && normalizedCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static ResponseCookie buildCookie(String cookieName, String cookieValue, long maxAgeSeconds, boolean secure) {
        String normalizedCookieName = Objects.requireNonNull(cookieName, "cookieName must not be null").trim();
        String normalizedCookieValue = cookieValue == null ? "" : cookieValue;
        long normalizedMaxAge = Math.max(maxAgeSeconds, 1L);
        return ResponseCookie.from(normalizedCookieName, normalizedCookieValue)
                .httpOnly(true)
                .secure(secure)
                .path(COOKIE_PATH)
                .maxAge(normalizedMaxAge)
                .sameSite(SAME_SITE_POLICY)
                .build();
    }

    public static ResponseCookie buildClearedCookie(String cookieName, boolean secure) {
        String normalizedCookieName = Objects.requireNonNull(cookieName, "cookieName must not be null").trim();
        return ResponseCookie.from(normalizedCookieName, "")
                .httpOnly(true)
                .secure(secure)
                .path(COOKIE_PATH)
                .maxAge(0)
                .sameSite(SAME_SITE_POLICY)
                .build();
    }
}
