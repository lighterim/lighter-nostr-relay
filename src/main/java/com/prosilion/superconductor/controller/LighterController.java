package com.prosilion.superconductor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosilion.superconductor.service.event.EventServiceIF;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.impl.TradeMessageEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.tag.CreatedByTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/lighter")
public class LighterController<T extends BaseMessage> {

    @Autowired
    private EventServiceIF<EventMessage> eventService;


    @PostMapping("/pushTradeMessage")
    public Map<String, String> pushTradeMessage(@RequestBody String json) {
        try {
            T message = (T) new BaseMessageDecoder<>().decode(json);
            if (message instanceof EventMessage eventMessage) {
                eventService.processIncomingEvent(eventMessage);
            }
            Map<String, String> resp = new HashMap<>();
            resp.put("status", "0");
            return resp;
        }
        catch (Exception ex){
            Map<String, String> resp = new HashMap<>();
            resp.put("status", "1");
            resp.put("message", ex.getMessage());
            return resp;
        }
    }
}
