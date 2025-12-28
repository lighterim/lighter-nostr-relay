package com.prosilion.superconductor.util;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private ErrorCode code;
    private String message;

    public BusinessException(ErrorCode errorCode) {
        super();
        this.code = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super();
        this.code = errorCode;
        this.message = message;
    }
}
