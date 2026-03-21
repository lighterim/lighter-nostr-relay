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
    TRADE_ID_NOT_FOUND(1003, "TradeId不存在"),
    SIG_SIGN_ERROR(1004, "Sig校验错误"),

    WISE_VERIFIER_PAYMENT_FAIL(2001, "wise verifier payment fail status"),
    WISE_VERIFIER_PAYMENT_ZERO_OR_REFUND(2002, "wise verifier payment zero or refund status"),
    WISE_VERIFIER_DOUBLE_SPENT(2003, "wise verifier double spent"),
    WISE_VERIFIER_REFERENCE_NONE(2004, "wise verifier reference none"),
    WISE_VERIFIER_TRADE_NOT_FOUND_OR_STATUS_ERROR(2005, "wise verifier trade not found or status error"),
    WISE_VERIFIER_PAYMENT_BEFORE_TRADE(2006, "wise verifier payment before trade"),
    WISE_VERIFIER_PAYMENT_CURRENCY_INCORRECT(2007, "wise verifier currency incorrect"),
    WISE_VERIFIER_PAYMENT_INSUFFICIENT(2008, "wise verifier insufficient"),
    WISE_VERIFIER_RECIPIENT_NOT_FOUND(2009, "wise verifier recipient not found"),
    WISE_VERIFIER_RECIPIENT_NOT_MATCH(2010, "wise verifier recipient not match"),


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
