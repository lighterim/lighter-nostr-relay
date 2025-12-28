package com.prosilion.superconductor.service.message.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.superconductor.service.event.EventServiceIF;
import com.prosilion.superconductor.service.clientresponse.ClientResponseService;
import com.prosilion.superconductor.service.message.MessageService;
import com.prosilion.superconductor.util.BusinessException;
import com.prosilion.superconductor.util.ErrorCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.message.EventMessage;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Map;

@Slf4j
public class EventMessageService<T extends EventMessage> implements MessageService<T> {
  @Getter
  public final String command = "EVENT";
  private final EventServiceIF<T> eventService;
  private final ClientResponseService clientResponseService;

  public EventMessageService(EventServiceIF<T> eventService, ClientResponseService clientResponseService) {
    this.eventService = eventService;
    this.clientResponseService = clientResponseService;
  }

  public void processIncoming(@NonNull T eventMessage, @NonNull String sessionId) {
    try {
      eventService.processIncomingEvent(eventMessage);
    } catch(Exception exception) {
      ErrorCode errorCode;
      String errorMsg = null;
      if (exception instanceof BusinessException bizException) {
        // 自定义异常
        errorCode = bizException.getCode();
        errorMsg = bizException.getMessage();
      } else if (exception instanceof IllegalArgumentException) {
        // 参数异常
        errorCode = ErrorCode.INVALID_MESSAGE;
      } else {
        errorCode = ErrorCode.INTERNAL_ERROR;
        // 生产环境隐藏详细错误信息
        if (!isProduction()) {
          errorMsg = String.format("%s: %s", exception.getClass().getName(), exception.getMessage());
        }
      }
      String errorMessage = String.format("%d: %s", errorCode.getCode(), errorMsg == null ? errorCode.getMessage() : errorMsg);
      clientResponseService.processNotOkClientResponse(sessionId, new EventMessage(eventMessage.getEvent()), errorMessage);
    }
  }

  protected void processOkClientResponse(@NonNull T eventMessage, @NonNull String sessionId) {
    clientResponseService.processOkClientResponse(sessionId, eventMessage);
  }

  protected void processNotOkClientResponse(@NonNull T eventMessage, @NonNull String sessionId, @NonNull String errorMessage) {
    clientResponseService.processNotOkClientResponse(sessionId, new EventMessage(eventMessage.getEvent()), errorMessage);
  }

  private boolean isProduction() {
    String env = System.getProperty("spring.profiles.active", "");
    return "prod".equals(env) || "production".equals(env);
  }
}