package com.prosilion.superconductor.controller;

import com.prosilion.superconductor.entity.AccountMessageEntity;
import com.prosilion.superconductor.service.event.AccountMessageEntityService;
import com.prosilion.superconductor.service.event.EventServiceIF;
import com.prosilion.superconductor.service.event.ProfileEntityService;
import com.prosilion.superconductor.service.http.webhook.TlsnVerifierService;
import jakarta.annotation.Resource;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import nostr.base.UserProfile;
import nostr.event.BaseMessage;
import nostr.event.impl.MetadataEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
public class LighterController<T extends BaseMessage> {

    @Autowired
    private EventServiceIF<EventMessage> eventService;

    @Autowired
    private ProfileEntityService profileEntityService;

    static final Pattern LOCAL_PART_PATTERN = Pattern.compile("^[a-z0-9_]+$", Pattern.CASE_INSENSITIVE);

    @Autowired
    AccountMessageEntityService accountMessageEntityService;

    @Resource
    TlsnVerifierService tlsnVerifierService;

    @GetMapping("/lighter/account/{nostrPubkey}")
    public Map<String, Object> getAccountByPubkey(@PathVariable String nostrPubkey) {
        return getAccount("nostrPubkey", null, nostrPubkey, null, null);
    }

    @GetMapping("/lighter/account/nft/{chainId}/{nftId}")
    public Map<String, Object> getAccountByChainIdAndNftId(@PathVariable BigInteger chainId, @PathVariable String nftId) {
        return getAccount("nftId", chainId, null, nftId, null);
    }

    @GetMapping("/lighter/account/tba/{chainId}/{tba}")
    public Map<String, Object> getAccountByChainIdAndTba(@PathVariable BigInteger chainId, @PathVariable String tba) {
        return getAccount("tba", chainId, null, null, tba);
    }

    @PostMapping("/lighter/verifierWebHook")
    public Map<String, Object> verifierWebHook(@RequestBody String payload) {
        log.info("payload: {}", payload);
        BaseMessage message = tlsnVerifierService.verifyTlsnProof(payload);
        if(message != null && message instanceof EventMessage eventMessage) {
            eventService.processIncomingEvent(eventMessage);
        }
        else{
            log.warn("verifyTlsnProof failure: {}", message);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "0");
        return resp;
    }

    @PostMapping("/lighter/pushTradeMessage")
    public Map<String, Object> pushTradeMessage(@RequestBody String json) {
        try {
            T message = (T) new BaseMessageDecoder<>().decode(json);
            if (message instanceof EventMessage eventMessage) {
                eventService.processIncomingEvent(eventMessage);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "0");
            return resp;
        }
        catch (Exception ex){
            log.warn(ex.getMessage(), ex);
            return reportErrorMessage(ex.getMessage());
        }
    }

    @PostMapping("/lighter/nftMinted")
    public Map<String, Object> nftMinted(@RequestBody String json){
        try{
            T message = (T) new BaseMessageDecoder<>().decode(json);
//            if (message instanceof EventMessage eventMessage) {
//                eventService.processIncomingEvent(eventMessage);
//            }
            log.info("{}", message);
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "0");
            return resp;
        }
        catch (Throwable ex){
            log.warn(ex.getMessage(), ex);
            return reportErrorMessage(ex.getMessage());
        }
    }

    @NotNull
    private static Map<String, Object> reportErrorMessage(String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "1");
        resp.put("message", message);
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

    private Map<String, Object> getAccount(String type, BigInteger chainId, String nostrPubkey, String nftId, String tba) {
        AccountMessageEntity accountMessageEntity = null;
        if("nostrPubkey".equals(type)) {
            accountMessageEntity = accountMessageEntityService.getByNostrPubkey(nostrPubkey);
        } else if("nftId".equals(type)) {
            accountMessageEntity = accountMessageEntityService.getByChainIdAndNftId(chainId, nftId);
        } else if("tba".equals(type)) {
            accountMessageEntity = accountMessageEntityService.getByChainIdAndTba(chainId, tba);
        }
        if(accountMessageEntity==null) {
            return reportErrorMessage("account not found");
        }
        accountMessageEntity.setIpfsHash(String.format("https://ipfs.io/ipfs/%s", accountMessageEntity.getIpfsHash()));
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "0");
        resp.put("account", accountMessageEntity);
        return resp;
    }

}
