package com.prosilion.superconductor.util;

public enum ErrorCode {

    SUCCESS(200, "成功"),
    INVALID_MESSAGE(400, "无效的消息格式"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),
    JSON_PROCESS_ERROR(422, "JSON解析异常"),
    SERVICE_NOTWORK_ERROR(503, "服务暂时不可用"),
    BUSINESS_ERROR(1000, "业务错误"),
    COMMON_ERROR(1001, "未知错误"),
    PARAM_ERROR(1002, "参数错误"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorCode getInstance(int code){
        for(ErrorCode instance : ErrorCode.values()){
            if(instance.getCode() == code){
                return instance;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
