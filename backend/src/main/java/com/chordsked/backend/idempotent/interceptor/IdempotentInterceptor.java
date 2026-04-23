package com.chordsked.backend.idempotent.interceptor;

import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.config.properties.IdempotentProperties;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.idempotent.annotation.Idempotent;
import com.chordsked.backend.idempotent.filter.CachedBodyHttpServletRequest;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Component("idempotentInterceptor")
public class IdempotentInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(IdempotentInterceptor.class);
    private static final DefaultRedisScript<Long> IDEMPOTENT_LUA_SCRIPT = buildIdempotentLuaScript();
    private static final String LUA_LOCK_VALUE = "1";

    @Resource(name = "redisProperties")
    private RedisProperties redisProperties;

    @Resource(name = "idempotentProperties")
    private IdempotentProperties idempotentProperties;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);
        if (idempotent == null) {
            return true;
        }
        if (!redisProperties.isEnabled() || !idempotentProperties.isEnabled()) {
            return true;
        }

        String idempotentKey = buildIdempotentKey(request);
        long expireSeconds = resolveExpireSeconds(idempotent.expireSeconds());
        try {
            Long result = stringRedisTemplate.execute(
                    IDEMPOTENT_LUA_SCRIPT,
                    Collections.singletonList(idempotentKey),
                    LUA_LOCK_VALUE,
                    String.valueOf(expireSeconds)
            );
            if (!Objects.equals(result, 1L)) {
                throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, idempotent.message());
            }
            return true;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            logger.warn("Idempotent check failed, key={}, fallback to pass", idempotentKey, exception);
            return true;
        }
    }

    private String buildIdempotentKey(HttpServletRequest request) {
        String userIdentity = resolveUserIdentity();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String bodyHash = resolveBodyHash(request);
        return idempotentProperties.getKeyPrefix()
                + userIdentity
                + ":"
                + method
                + ":"
                + uri
                + ":"
                + bodyHash;
    }

    private String resolveUserIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof ChordSkedUserDetails userDetails)) {
            return "anonymous";
        }
        if (userDetails.getUserId() == null || userDetails.getUserId() <= 0L) {
            return "anonymous";
        }
        return userDetails.getUserType() + ":" + userDetails.getUserId();
    }

    private String resolveBodyHash(HttpServletRequest request) {
        byte[] body = resolveBodyBytes(request);
        if (body.length == 0) {
            body = resolveParameterBytes(request);
        }
        return DigestUtils.md5DigestAsHex(body);
    }

    private byte[] resolveBodyBytes(HttpServletRequest request) {
        if (!(request instanceof CachedBodyHttpServletRequest cachedRequest)) {
            return new byte[0];
        }
        return cachedRequest.getCachedBody();
    }

    private byte[] resolveParameterBytes(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap == null || parameterMap.isEmpty()) {
            return new byte[0];
        }
        Map<String, String[]> sortedParameterMap = new TreeMap<>(parameterMap);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String[]> entry : sortedParameterMap.entrySet()) {
            builder.append(entry.getKey()).append('=');
            String[] values = entry.getValue();
            if (values != null) {
                for (String value : values) {
                    builder.append(value).append(',');
                }
            }
            builder.append('&');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private long resolveExpireSeconds(long expireSecondsFromAnnotation) {
        if (expireSecondsFromAnnotation > 0L) {
            return expireSecondsFromAnnotation;
        }
        return Math.max(idempotentProperties.getExpireSeconds(), 1L);
    }

    private static DefaultRedisScript<Long> buildIdempotentLuaScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
                "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then "
                        + "redis.call('expire', KEYS[1], ARGV[2]); "
                        + "return 1; "
                        + "end; "
                        + "return 0;"
        );
        return script;
    }
}
