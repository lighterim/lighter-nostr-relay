package com.prosilion.superconductor.controller;

import com.google.gson.Gson;
import com.prosilion.superconductor.http.body.AddressBook;
import com.prosilion.superconductor.http.body.Intents;
import com.prosilion.superconductor.http.body.Rate;
import com.prosilion.superconductor.http.body.Trades;
import com.prosilion.superconductor.service.event.EventServiceIF;
import com.prosilion.superconductor.service.event.IntentEntityService;
import com.prosilion.superconductor.service.event.TradeEntityService;
import com.prosilion.superconductor.service.event.TradeMessageEntityService;
import com.prosilion.superconductor.util.BusinessException;
import com.prosilion.superconductor.util.ErrorCode;
import com.prosilion.superconductor.util.TagUtil;
import lombok.extern.slf4j.Slf4j;
import nostr.base.IEvent;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.BaseMessage;
import nostr.event.Kind;
import nostr.event.impl.AddressBookIntentEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.util.NostrUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/evm")
public class EvmController<T extends BaseMessage> {

    @Autowired
    private EventServiceIF<EventMessage> eventService;

    @Autowired
    private IntentEntityService intentEntityService;

    @Autowired
    private TradeEntityService tradeEntityService;

    @Autowired
    private TradeMessageEntityService tradeMessageEntityService;

    @Value("#{${rate.bp}}")
    private Map<String, Integer> rateBP;

    @PostMapping("/intents")
    public Map<String, Object> listIntent(@RequestBody Intents intents) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", 0);
        resp.put("data", intentEntityService.getAllAsList(intents));
        return resp;
    }

    @PostMapping("/trades")
    public Map<String, Object> listTrade(@RequestBody Trades trades, @RequestHeader(value = "X-Nostr-Sig") String sig) {
        boolean verify = true;//tradeEntityService.verifyTradesSig(sig, trades);
        Map<String, Object> resp = new HashMap<>();
        if(!verify) {
            resp.put("status", 1001);
            resp.put("message", "sig verify fail.");
            return resp;
        }
        resp.put("status", 0);
        resp.put("data", tradeEntityService.getAllasList(trades));
        return resp;
    }

    @PostMapping("/trade/messages")
    public Map<String, Object> listTradeMessage(@RequestBody Trades trades, @RequestHeader(value = "X-Nostr-Sig") String sig) {
        boolean verify = true;//tradeEntityService.verifyTradesSig(sig, trades);
        Map<String, Object> resp = new HashMap<>();
        if(!verify) {
            resp.put("status", 1001);
            resp.put("message", "sig verify fail.");
            return resp;
        }
        resp.put("status", 0);
        resp.put("data", tradeEntityService.getAllasList(trades));
        return resp;
    }

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
