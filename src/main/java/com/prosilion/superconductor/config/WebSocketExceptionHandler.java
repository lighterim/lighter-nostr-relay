package com.prosilion.superconductor.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosilion.superconductor.service.okresponse.ExResponse;
import com.prosilion.superconductor.util.BusinessException;
import com.prosilion.superconductor.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WebSocketExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 处理WebSocket异常并发送错误消息
     */
    public void handleException(Throwable throwable, WebSocketSession session) {
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket会话已关闭，无法发送错误信息");
            return;
        }

        try {
            ExResponse<Object> response = buildErrorResponse(throwable);
            sendErrorMessage(response, session);

        } catch (Exception e) {
            log.error("处理WebSocket异常时发生错误", e);
        }
    }

    /**
     * 构建错误响应
     */
    private ExResponse<Object> buildErrorResponse(Throwable throwable) {
        ErrorCode errorCode;
        Object errorData = null;

        if (throwable instanceof BusinessException bizException) {
            // 自定义异常
            errorCode = bizException.getCode();
            errorData = bizException.getData();

        } else if (throwable instanceof IllegalArgumentException) {
            // 参数异常
            errorCode = ErrorCode.INVALID_MESSAGE;

        } else if (throwable instanceof AuthenticationException) {
            // 认证异常
            errorCode = ErrorCode.UNAUTHORIZED;

        } else if (throwable instanceof AccessDeniedException) {
            // 权限异常
            errorCode = ErrorCode.FORBIDDEN;

        } else if (throwable instanceof JsonProcessingException) {
            // JSON解析异常
            errorCode = ErrorCode.JSON_PROCESS_ERROR;

        } else if (throwable instanceof IOException) {
            // IO异常
            errorCode = ErrorCode.SERVICE_NOTWORK_ERROR;
        } else {
            // 其他未知异常
            errorCode = ErrorCode.INTERNAL_ERROR;
            // 生产环境隐藏详细错误信息
            if (!isProduction()) {
                errorData = Map.of(
                        "exception", throwable.getClass().getName(),
                        "detail", throwable.getMessage()
                );
            }
        }

        logError(throwable, errorCode);

        return new ExResponse<>(errorCode, errorData);
    }

    /**
     * 发送错误消息到客户端
     */
    private void sendErrorMessage(ExResponse<Object> response, WebSocketSession session)
            throws IOException {

        // 可以添加消息类型标识
        Map<String, Object> enhancedResponse = new HashMap<>();
        enhancedResponse.put("code", response.getErrorCode().getCode());
        enhancedResponse.put("message", response.getErrorCode().getMessage());
        enhancedResponse.put("data", response.getData());

        String finalJson = objectMapper.writeValueAsString(enhancedResponse);
        session.sendMessage(new TextMessage(finalJson));

        log.debug("已发送错误消息到会话 {}: {}", session.getId(), response);
    }

    /**
     * 记录错误日志
     */
    private void logError(Throwable throwable, ErrorCode errorCode) {
        int code = errorCode.getCode();
        if (code >= 500) {
            // 服务器错误记录完整堆栈
            log.error("WebSocket服务器错误[{}]: {} - {}",
                    code, errorCode.getMessage(), throwable.getMessage(), throwable);
        } else if (code >= 400) {
            // 客户端错误记录警告
            log.warn("WebSocket客户端错误[{}]: {} - {}",
                    code, errorCode.getMessage(), throwable.getMessage());
        } else {
            log.info("WebSocket异常[{}]: {}", code, errorCode.getMessage());
        }
    }

    /**
     * 处理批量异常（如果有多个会话需要处理相同异常）
     */
    public void handleBatchException(Throwable throwable, List<WebSocketSession> sessions) {
        if (CollectionUtils.isEmpty(sessions)) {
            return;
        }

        ExResponse<Object> response = buildErrorResponse(throwable);

        sessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    sendErrorMessage(response, session);
                } catch (IOException e) {
                    log.warn("发送批量错误消息失败，会话ID: {}", session.getId(), e);
                }
            }
        });
    }

    /**
     * 判断是否是生产环境
     */
    private boolean isProduction() {
        String env = System.getProperty("spring.profiles.active", "");
        return "prod".equals(env) || "production".equals(env);
    }
}
