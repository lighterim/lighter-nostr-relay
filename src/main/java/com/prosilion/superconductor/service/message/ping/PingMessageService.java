package com.prosilion.superconductor.service.message.ping;

import com.prosilion.superconductor.service.clientresponse.ClientResponseService;
import com.prosilion.superconductor.service.message.MessageService;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
import nostr.event.message.PingMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PingMessageService<T extends PingMessage> implements MessageService<T> {

    ClientResponseService clientResponseService;
    public PingMessageService(ClientResponseService clientResponseService) {
        this.clientResponseService = clientResponseService;
    }

    @Override
    public void processIncoming(T reqMessage, String sessionId) {
        clientResponseService.processPongClientResponse(sessionId);
    }

    @Override
    public String getCommand() {
        return Command.PING.name();
    }
}
