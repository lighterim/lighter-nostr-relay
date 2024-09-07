package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.base.GenericTagQuery;
import nostr.event.Kind;
import nostr.event.impl.*;
import nostr.event.query.CompositionQuery;
import nostr.event.tag.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;

import static nostr.event.Kind.TEXT_NOTE;

@Component
public class FilterCompositionQueryPlugin<T extends CompositionQuery> implements FilterPlugin<T> {

  @Override
  public BiPredicate<T, AddNostrEvent<GenericEvent>> getBiPredicate() {
    return (t, u) -> {
      GenericEvent event = u.event();
      Kind kind = event.getKind() == null ? TEXT_NOTE : Kind.valueOf(event.getKind());
      if (kind != t.getKind()) {
        return false;
      }

      for (GenericTagQuery tag : t.getAnyMatchList()) {
        String tagName = tag.getTagName();
        List<String> values = tag.getValue();
          return switch (kind) {
              case POST_INTENT -> getBiPredicate(tagName, values, (PostIntentEvent) event);
              case TAKE_INTENT -> getBiPredicate(tagName, values, (TakeIntentEvent) event);
              case TRADE_MESSAGE -> getBiPredicate(tagName, values, (TradeMessageEvent) event);
              default -> false;
          };
      }

      return false;
    };
  }

  private boolean getBiPredicate(String tagName, List<String> value, TradeMessageEvent event) {
    if("eventIdString".equals(tagName)){
      return value.contains(event.getCreatedByTag().getTakeIntentEventId());
    }
    return false;
  }

  private boolean getBiPredicate(String tagName, List<String> value, TakeIntentEvent event) {
    TakeTag takeTag = event.getTakeTag();
    if("pubkey".equals(tagName)){
      return value.contains(takeTag.getMakerPubkey()) || value.contains(takeTag.getTakerPubkey());
    }
    else if("nip05".equals(tagName)){
      return value.contains(takeTag.getMakerNip05()) || value.contains(takeTag.getTakerNip05());
    }
    return false;
  }

  private boolean getBiPredicate(String tagName, List<String> value, PostIntentEvent event) {
    if (tagName.equals("side")) {
      MakeTag makeTag = event.getSideTag();
      return makeTag != null && value.contains(makeTag.getSide().getSide());
    } else if (tagName.equals("symbol")) {
      TokenTag token = event.getTokenTag();
      return token != null && value.contains(token.getSymbol());
    } else if (tagName.equals("currency")) {
      QuoteTag quote = event.getQuoteTag();
      return quote != null && value.contains(quote.getCurrency());
    } else if (tagName.equals("paymentMethod")) {
      List<PaymentTag> paymentMethods = event.getPaymentTags();
      for (PaymentTag p : paymentMethods) {
        if (value.contains(p.getMethod())) {
          return true;
        }
      }
      return false;
    }
    return false;
  }


  @Override
  public List<T> getPluginFilters(Filters filters) {
    return (List<T>) List.of(filters.getCompositionQuery());
  }

  @Override
  public String getCode() {
    return "composition";
  }

}
