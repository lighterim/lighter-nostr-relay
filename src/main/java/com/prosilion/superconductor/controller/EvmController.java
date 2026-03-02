package com.prosilion.superconductor.controller;

import com.google.gson.Gson;
import com.prosilion.superconductor.http.body.AddressBook;
import com.prosilion.superconductor.http.body.Rate;
import com.prosilion.superconductor.service.event.EventServiceIF;
import com.prosilion.superconductor.util.TagUtil;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.Kind;
import nostr.event.impl.AddressBookIntentEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/evm")
public class EvmController<T extends BaseMessage> {

    @Autowired
    private EventServiceIF<EventMessage> eventService;

    @Value("#{${rate.bp}}")
    private Map<String, Integer> rateBP;

    @PostMapping("/rate/get")
    public Map<String, Integer> getRate(@RequestBody Rate rate) {
        Integer rateValue = null;
        if(rate.getSide()!=null) {
            rateValue = rateBP.get(rate.getSide().name());
        } else if(rate.getRole()!=null) {
            rateValue = rateBP.get(rate.getRole().name());
        }
        Map<String, Integer> resp = new HashMap<>();
        resp.put("status", 0);
        resp.put("rate", rateValue);
        return resp;
    }
    @PostMapping("/addressbook/add")
    public Map<String, String> pushAddressBookMessage(@RequestBody AddressBook addressBook) {
        Gson gson = new Gson();
        Map<String, Object> event = new LinkedHashMap<>();

        int kind = Kind.ADDRESS_BOOK_INTENT.getValue();
        long ts = System.currentTimeMillis();
        String digestContent = String.format("[[%d,%s,%d,%s,%s]]",
                ts,
                addressBook.getPubkey(),
                kind,
                addressBook.getCreatedBy(),
                addressBook.getAddress());

        event.put("id", TagUtil.createDigest(digestContent));
        event.put("kind", kind);
        event.put("content", AddressBookIntentEvent.ADDRESS_BOOK_TAG_CODE);
        event.put("tags", Arrays.asList(
                Arrays.asList("address_book", addressBook.getName(), addressBook.getAddress(),
                        addressBook.getPubkey(),
                        addressBook.getNftId(),
                        addressBook.getChainId(), addressBook.getCreatedBy())));
        event.put("pubkey", addressBook.getPubkey());
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
