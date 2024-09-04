package com.prosilion.superconductor.controller;

import lombok.extern.slf4j.Slf4j;
import nostr.event.impl.TradeMessageEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/lighter")
public class LighterController {

    @PostMapping("/pushTradeMessage")
    public Map<String, String> pushTradeMessage(@RequestBody TradeMessageEvent messageEvent) {

        Map<String, String> resp = new HashMap<>();
        resp.put("hello", "world");
        return resp;
    }
}
