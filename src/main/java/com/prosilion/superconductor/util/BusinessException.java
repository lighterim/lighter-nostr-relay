package com.prosilion.superconductor.util;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private ErrorCode code;
    private Object data;

    public BusinessException(ErrorCode errorCode) {
        super();
        this.code = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Object data) {
        super();
        this.code = errorCode;
        this.data = data;
    }
}
