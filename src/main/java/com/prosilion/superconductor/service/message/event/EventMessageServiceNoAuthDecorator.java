package com.prosilion.superconductor.service.message.event;

import com.prosilion.superconductor.service.event.EventServiceIF;
import com.prosilion.superconductor.service.clientresponse.ClientResponseService;
import com.prosilion.superconductor.service.message.MessageService;
import com.prosilion.superconductor.util.BusinessException;
import com.prosilion.superconductor.util.ErrorCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.message.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(
    name = "superconductor.auth.active",
    havingValue = "false")
public class EventMessageServiceNoAuthDecorator<T extends EventMessage> implements MessageService<T> {
  private final EventMessageService<T> eventMessageService;

  @Autowired
  public EventMessageServiceNoAuthDecorator(EventServiceIF<T> eventService, ClientResponseService okResponseService) {
    this.eventMessageService = new EventMessageService<>(eventService, okResponseService);
  }

  public void processIncoming(@NonNull T eventMessage, @NonNull String sessionId) {
    log.info("EVENT message NIP: {}", eventMessage.getNip());
    log.info("EVENT message type: {}", eventMessage.getEvent());
    try {
      eventMessageService.processIncoming(eventMessage, sessionId);
      eventMessageService.processOkClientResponse(eventMessage, sessionId);
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
      eventMessageService.processNotOkClientResponse(eventMessage, sessionId, errorMessage);
    }
  }

  @Override
  public String getCommand() {
    return eventMessageService.getCommand();
  }

  private boolean isProduction() {
    String env = System.getProperty("spring.profiles.active", "");
    return "prod".equals(env) || "production".equals(env);
  }
}