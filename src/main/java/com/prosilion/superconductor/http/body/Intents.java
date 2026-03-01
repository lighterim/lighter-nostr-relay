package com.prosilion.superconductor.http.body;

import lombok.Data;

@Data
public class Intents {

    private Long id;

    private String pubKey;

    private Integer status;

    private String eventId;
}
