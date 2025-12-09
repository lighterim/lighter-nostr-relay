package com.prosilion.superconductor.service.okresponse;

import com.prosilion.superconductor.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExResponse<T> {
    private ErrorCode errorCode;
    private T data;

    public static <T> ExResponse<T> success(T data) {
        return new ExResponse<>(ErrorCode.SUCCESS, data);
    }

    public static ExResponse<Object> error(ErrorCode errorCode) {
        return new ExResponse<>(errorCode, null);
    }
}
