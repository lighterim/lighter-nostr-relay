package com.prosilion.superconductor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosilion.superconductor.service.event.EventServiceIF;
import com.prosilion.superconductor.service.event.ProfileEntityService;
import jakarta.annotation.Resource;
import jakarta.persistence.NoResultException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.base.UserProfile;
import nostr.event.BaseMessage;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.TradeMessageEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.tag.CreatedByTag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
public class LighterController<T extends BaseMessage> {

    @Autowired
    private EventServiceIF<EventMessage> eventService;

    @Value("${nip05.domain:@lighter.im}")
    private String nip05Domain;

    @Autowired
    private ProfileEntityService profileEntityService;

    static final Pattern LOCAL_PART_PATTERN = Pattern.compile("^[a-z0-9_]+$", Pattern.CASE_INSENSITIVE);


    @PostMapping("/lighter/pushTradeMessage")
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
            log.warn(ex.getMessage(), ex);
            return reportErrorMessage(ex);
        }
    }

    @PostMapping("/lighter/nftMinted")
    public Map<String, String> nftMinted(@RequestBody String json){
        try{
            T message = (T) new BaseMessageDecoder<>().decode(json);
//            if (message instanceof EventMessage eventMessage) {
//                eventService.processIncomingEvent(eventMessage);
//            }
            log.info("{}", message);
            Map<String, String> resp = new HashMap<>();
            resp.put("status", "0");
            return resp;
        }
        catch (Throwable ex){
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

    @GetMapping("/.well-known/nostr.json")
    public Map<String, Object> nip05(@RequestParam("name") String name){
        Map<String, Object> resp = new HashMap<>();
        if(!StringUtils.hasText(name) || !LOCAL_PART_PATTERN.matcher(name).matches()){
            resp.put("status", "1");
            resp.put("message", "invalid <local-part>: "+name);
            return resp;
        }

        try {
            MetadataEvent event = profileEntityService.getEventByNip05(name);
//        UserProfile userProfile = UserProfile.builder().nip05(name).publicKey(new PublicKey("aaad79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984")).build();
            UserProfile userProfile = event.getProfile();
            Map<String, String> row = new HashMap<>();
            row.put(userProfile.getNip05(), userProfile.getPublicKey().toString());
            resp.put("name", row);
            return resp;
        }
        catch (NoResultException ex){
            log.warn("NoResultException:{}", name);
            resp.clear();
            resp.put("status", "1");
            resp.put("message", "invalid <local-part>: "+name);
        }
        catch (Throwable ex){
            log.warn(ex.getMessage(), ex);
            resp.clear();
            resp.put("status", "1");
            resp.put("message", ex.getMessage());
        }
        return resp;
    }

}
