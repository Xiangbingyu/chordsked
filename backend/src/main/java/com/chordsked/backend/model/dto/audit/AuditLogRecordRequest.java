package com.chordsked.backend.model.dto.audit;

public class AuditLogRecordRequest {
    private String moduleName;
    private String actionType;
    private Long bizId;
    private Long userId;
    private String userName;
    private String userType;
    private String requestUri;
    private String requestMethod;
    private String requestIp;
    private String userAgent;
    private String requestParams;
    private String responseResult;
    private Integer status;
    private String errorMsg;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseResult() {
        return responseResult;
    }

    public void setResponseResult(String responseResult) {
        this.responseResult = responseResult;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public static AuditLogRecordRequest copyOf(AuditLogRecordRequest source) {
        AuditLogRecordRequest target = new AuditLogRecordRequest();
        if (source == null) {
            return target;
        }
        target.setModuleName(source.getModuleName());
        target.setActionType(source.getActionType());
        target.setBizId(source.getBizId());
        target.setUserId(source.getUserId());
        target.setUserName(source.getUserName());
        target.setUserType(source.getUserType());
        target.setRequestUri(source.getRequestUri());
        target.setRequestMethod(source.getRequestMethod());
        target.setRequestIp(source.getRequestIp());
        target.setUserAgent(source.getUserAgent());
        target.setRequestParams(source.getRequestParams());
        target.setResponseResult(source.getResponseResult());
        target.setStatus(source.getStatus());
        target.setErrorMsg(source.getErrorMsg());
        return target;
    }
}
