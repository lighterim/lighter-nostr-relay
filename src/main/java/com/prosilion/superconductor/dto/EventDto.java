package com.prosilion.superconductor.dto;

import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.IntentEventEntity;
import com.prosilion.superconductor.entity.TakeIntentEventEntity;
import com.prosilion.superconductor.entity.TradeMessageEntity;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.*;
import nostr.event.impl.PostIntentEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.impl.TradeMessageEvent;
import nostr.event.tag.*;

import java.math.BigDecimal;
import java.util.List;

public class EventDto extends NIP01Event {

  public EventDto(PublicKey pubKey, String eventId, Kind kind, Integer nip, Long createdAt, Signature signature, List<BaseTag> tags, String content) {
    super(pubKey, kind, tags);
    setId(eventId);
    setNip(nip);
    setCreatedAt(createdAt);
    setSignature(signature);
    setContent(content);
  }

  public EventEntity convertDtoToEntity() {
    return new EventEntity(getId(), getKind(), getNip(), getPubKey().toString(), getCreatedAt(), getSignature().toString(), getContent());
  }

  public static IntentEventEntity convertToEntity(PostIntentEvent event){
    MakeTag make = event.getSideTag();
    TokenTag token = event.getTokenTag();
    QuoteTag quote = event.getQuoteTag();
    LimitTag limit = event.getLimitTag();

    return new IntentEventEntity(
            make.getSide().getSide(),
            make.getMakerNip05(),
            event.getPubKey().toString(),

            token.getSymbol(),
            token.getChain(),
            token.getNetwork(),
            token.getAddress(),
            token.getAmount(),

            quote.getNumber(),
            quote.getCurrency(),

            limit==null?null:limit.getCurrency(),
            limit==null||limit.getLowLimit()==null?null:limit.getLowLimit(),
            limit==null||limit.getUpLimit()==null?null:limit.getUpLimit(),

            event.getSignature().toString(),
            event.getId(),
            event.getKind(),
            event.getNip(),
            event.getCreatedAt(),

            event.getContent()

    );
  }

  public static TakeIntentEventEntity convertToEntity(TakeIntentEvent event){
    TakeTag takeTag = event.getTakeTag();
    TokenTag tokenTag = event.getTokenTag();
    PaymentTag paymentTag = event.getPaymentTag();
    QuoteTag quoteTag = event.getQuoteTag();
    BigDecimal volume = takeTag.getVolume();

    String buyerId;
    String buyerPubKey;
    String sellerId;
    String sellerPubKey;

    if (takeTag.getSide() == Side.BUY) {
      buyerId = takeTag.getTakerNip05();
      buyerPubKey = takeTag.getTakerPubkey();
      sellerId = takeTag.getMakerNip05();
      sellerPubKey = takeTag.getMakerPubkey();
    } else {
      buyerId = takeTag.getMakerNip05();
      buyerPubKey = takeTag.getMakerPubkey();
      sellerId = takeTag.getTakerNip05();
      sellerPubKey = takeTag.getTakerPubkey();
    }


    return  new TakeIntentEventEntity(
            event.getNip(),
            event.getKind(),
            event.getId(),
            takeTag.getSide().getSide(),
            takeTag.getIntentEventId(),
            volume,
            buyerId,
            buyerPubKey,
            sellerId,
            sellerPubKey,
            tokenTag.getAddress(), tokenTag.getSymbol(), tokenTag.getChain(), tokenTag.getNetwork(),
            quoteTag.getNumber(),  quoteTag.getCurrency(), quoteTag.getUsdRate(),
            paymentTag.getMethod(), paymentTag.getAccount(), paymentTag.getQrCode(), paymentTag.getMemo(),
            event.getTradeStatus(),
            event.getContent(),
            event.getSignature().toString(),
            event.getCreatedAt()
    );
  }

  public static TradeMessageEntity convertToEntity(TradeMessageEvent event){
    CreatedByTag createdByTag = event.getCreatedByTag();
    return new TradeMessageEntity(
            event.getNip(),
            event.getKind(),
            event.getId(),
            createdByTag.getTakeIntentEventId(),
            createdByTag.getNip05(),
            createdByTag.getPubkey(),
            event.getContent(),
            event.getSignature().toString(),
            event.getCreatedAt()
    );
  }
}
