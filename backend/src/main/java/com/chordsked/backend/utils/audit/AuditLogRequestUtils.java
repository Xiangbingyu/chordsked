package com.chordsked.backend.utils.audit;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuditLogRequestUtils {
    private AuditLogRequestUtils() {
    }

    public static AuditLogRecordRequest buildAuditLogRequest(HttpServletRequest httpServletRequest) {
        AuditLogRecordRequest request = new AuditLogRecordRequest();
        if (httpServletRequest != null) {
            request.setRequestUri(httpServletRequest.getRequestURI());
            request.setRequestMethod(httpServletRequest.getMethod());
            request.setRequestIp(httpServletRequest.getRemoteAddr());
            request.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return request;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ChordSkedUserDetails userDetails) {
            request.setUserId(userDetails.getUserId());
            request.setUserType(userDetails.getUserType());
            request.setUserName(userDetails.getUsername());
            return request;
        }
        request.setUserName(authentication.getName());
        return request;
    }
}
