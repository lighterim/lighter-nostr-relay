package com.prosilion.superconductor.controller;

import com.google.gson.Gson;
import com.prosilion.superconductor.entity.event.RemarkReq;
import com.prosilion.superconductor.service.event.EventServiceIF;
import com.prosilion.superconductor.service.event.ProfileEntityService;
import com.prosilion.superconductor.util.TagUtil;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import nostr.base.UserProfile;
import nostr.event.BaseMessage;
import nostr.event.Kind;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.RemarkIntentEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/evm")
public class EvmController<T extends BaseMessage> {

    @Autowired
    private EventServiceIF<EventMessage> eventService;

    @PostMapping("/pushRemarkMessage")
    public Map<String, String> pushRemarkMessage(@RequestBody RemarkReq remarkReq) {
        Gson gson = new Gson();
        Map<String, Object> event = new LinkedHashMap<>();

        int kind = Kind.REMARK_INTENT.getValue();
        long ts = System.currentTimeMillis();
        String digestContent = String.format("[[%d,%s,%d,%s,%s]]",
                ts,
                remarkReq.getPubkey(),
                kind,
                remarkReq.getCreatedBy(),
                remarkReq.getAddress());

        event.put("id", TagUtil.createDigest(digestContent));
        event.put("kind", kind);
        event.put("content", RemarkIntentEvent.REMARK_INTENT_EVENT);
        event.put("tags", Arrays.asList(
                Arrays.asList("remark", remarkReq.getName(), remarkReq.getAddress(),
                        remarkReq.getPubkey(),
                        remarkReq.getNftId(),
                        remarkReq.getChainId(), remarkReq.getCreatedBy())));
        event.put("pubkey", remarkReq.getPubkey());
        event.put("created_at", ts);

        Object[] eventObj = new Object[]{"EVENT", event};
        String json = gson.toJson(eventObj);

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
            log.warn(ex.getMessage(), ex);
            return reportErrorMessage(ex);
        }
    }


    @NotNull
    private static Map<String, String> reportErrorMessage(Throwable ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("status", "1");
        resp.put("message", ex.getMessage());
        return resp;
    }

}
