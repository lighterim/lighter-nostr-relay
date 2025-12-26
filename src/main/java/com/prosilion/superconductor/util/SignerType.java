package com.prosilion.superconductor.util;

public enum SignerType {

    POST_EVENT(1, "验证PostEvent"),

    PRICE(2, "验证价格"),

    TAKE_EVENT(3, "验证TakeEvent"),

    TRADE_EVENT(4, "验证TradeEvent"),
    ;

    private final int type;

    private final String desc;

    SignerType(int type, String desc){
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return this.type;
    }

    public String getDesc() {
        return this.desc;
    }
}
