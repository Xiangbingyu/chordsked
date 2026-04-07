package com.chordsked.backend.utils.cookie;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieUtilsTest {

    @Test
    void shouldWriteHttpOnlyCookieHeader() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtils.writeCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME, "token-value", 0, false);

        List<String> setCookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertEquals(1, setCookieHeaders.size());
        assertTrue(setCookieHeaders.get(0).startsWith("access_token=token-value"));
        assertTrue(setCookieHeaders.get(0).contains("Max-Age=1"));
        assertTrue(setCookieHeaders.get(0).contains("Path=/"));
        assertTrue(setCookieHeaders.get(0).contains("HttpOnly"));
        assertTrue(setCookieHeaders.get(0).contains("SameSite=Lax"));
    }

    @Test
    void shouldClearCookieHeader() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtils.clearCookie(response, CookieUtils.REFRESH_TOKEN_COOKIE_NAME, true);

        List<String> setCookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertEquals(1, setCookieHeaders.size());
        assertTrue(setCookieHeaders.get(0).startsWith("refresh_token="));
        assertTrue(setCookieHeaders.get(0).contains("Max-Age=0"));
        assertTrue(setCookieHeaders.get(0).contains("Path=/"));
        assertTrue(setCookieHeaders.get(0).contains("HttpOnly"));
        assertTrue(setCookieHeaders.get(0).contains("SameSite=Lax"));
        assertTrue(setCookieHeaders.get(0).contains("Secure"));
    }
}
