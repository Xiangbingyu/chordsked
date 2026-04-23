package com.chordsked.backend.audit.aspect;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.audit.provider.AuditLogPayloadExtractor;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.enums.AuditLogStatus;
import com.chordsked.backend.service.audit.AuditLogService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一处理 @AuditLog 审计记录，减少业务代码中的重复日志拼装逻辑。
 */
@Aspect
@Component("auditLogAspect")
public class AuditLogAspect {
    @Resource(name = "auditLogService")
    private AuditLogService auditLogService;

    @Autowired(required = false)
    private List<AuditLogPayloadExtractor> auditLogPayloadExtractors = java.util.Collections.emptyList();
    private Map<String, AuditLogPayloadExtractor> extractorRoute = java.util.Collections.emptyMap();

    @PostConstruct
    public void initializeExtractorRoute() {
        if (auditLogPayloadExtractors == null || auditLogPayloadExtractors.isEmpty()) {
            extractorRoute = java.util.Collections.emptyMap();
            return;
        }
        Map<String, AuditLogPayloadExtractor> route = new LinkedHashMap<>();
        for (AuditLogPayloadExtractor extractor : auditLogPayloadExtractors) {
            String routeKey = extractor.routeKey();
            AuditLogPayloadExtractor existing = route.putIfAbsent(routeKey, extractor);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate AuditLogPayloadExtractor routeKey: "
                                + routeKey
                                + ", existing="
                                + existing.getClass().getName()
                                + ", incoming="
                                + extractor.getClass().getName()
                );
            }
        }
        extractorRoute = java.util.Collections.unmodifiableMap(route);
    }

    @Around("@annotation(com.chordsked.backend.audit.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AuditLog annotation = method.getAnnotation(AuditLog.class);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        AuditLogRecordRequest baseRequest = buildBaseRequest(joinPoint.getArgs());
        baseRequest.setModuleName(annotation.module());
        baseRequest.setActionType(annotation.action());

        AuditLogContext successContext;
        try {
            Object result = joinPoint.proceed();
            successContext = new AuditLogContext(method, joinPoint.getArgs(), result);
            AuditLogRecordRequest successRequest = AuditLogRecordRequest.copyOf(baseRequest);
            successRequest.setStatus(AuditLogStatus.SUCCESS.getCode());
            fillByExtractorOnSuccess(successRequest, successContext);
            auditLogService.record(successRequest);
            return result;
        } catch (RuntimeException exception) {
            AuditLogContext failureContext = new AuditLogContext(method, joinPoint.getArgs(), null);
            AuditLogRecordRequest failedRequest = AuditLogRecordRequest.copyOf(baseRequest);
            failedRequest.setStatus(AuditLogStatus.FAILED.getCode());
            failedRequest.setErrorMsg(exception.getMessage());
            fillByExtractorOnFailure(failedRequest, failureContext, exception);
            auditLogService.record(failedRequest);
            throw exception;
        }
    }

    private AuditLogRecordRequest buildBaseRequest(Object[] args) {
        if (args == null || args.length == 0) {
            return new AuditLogRecordRequest();
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Method accessor = arg.getClass().getMethod("getAuditLogRequest");
                Object value = accessor.invoke(arg);
                if (value instanceof AuditLogRecordRequest request) {
                    return AuditLogRecordRequest.copyOf(request);
                }
            } catch (ReflectiveOperationException ignored) {
                // 当前参数不提供审计上下文，继续尝试下一个参数。
            }
        }
        return new AuditLogRecordRequest();
    }

    private void fillByExtractorOnSuccess(AuditLogRecordRequest request, AuditLogContext context) {
        AuditLogPayloadExtractor extractor = resolveExtractor(context);
        if (extractor != null) {
            extractor.fillOnSuccess(request, context);
            return;
        }
        Object result = context.getResult();
        if (result instanceof Number number) {
            request.setBizId(number.longValue());
        }
    }

    private void fillByExtractorOnFailure(
            AuditLogRecordRequest request,
            AuditLogContext context,
            RuntimeException exception
    ) {
        AuditLogPayloadExtractor extractor = resolveExtractor(context);
        if (extractor != null) {
            extractor.fillOnFailure(request, context, exception);
        }
    }

    private AuditLogPayloadExtractor resolveExtractor(AuditLogContext context) {
        if (context == null || context.getMethod() == null) {
            return null;
        }
        AuditLog annotation = context.getMethod().getAnnotation(AuditLog.class);
        if (annotation == null) {
            return null;
        }
        String routeKey = AuditLogPayloadExtractor.buildRouteKey(annotation.module(), annotation.action());
        return extractorRoute.get(routeKey);
    }
}
