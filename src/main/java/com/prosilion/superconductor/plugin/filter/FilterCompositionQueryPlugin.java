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

      final List<GenericTagQuery> anyMatchList = t.getAnyMatchList();
      return switch (kind) {
          case POST_INTENT -> getBiPredicate(anyMatchList, (PostIntentEvent) event);
          case TAKE_INTENT -> getBiPredicate(anyMatchList, (TakeIntentEvent) event);
          case TRADE_MESSAGE -> getBiPredicate(anyMatchList, (TradeMessageEvent) event);
          default -> false;
      };

    };
  }

  private boolean getBiPredicate(List<GenericTagQuery> anyMatchList, TradeMessageEvent event) {
    for(GenericTagQuery t: anyMatchList) {
      String tagName = t.getTagName();
      List<String> values = t.getValue();
      if ("eventIdString".equals(tagName)) {
        return values.contains(event.getCreatedByTag().getTakeIntentEventId());
      }
    }
    return false;
  }

  private boolean getBiPredicate(List<GenericTagQuery> anyMatchList, TakeIntentEvent event) {
    TakeTag takeTag = event.getTakeTag();
    for(GenericTagQuery t : anyMatchList) {
      String tagName = t.getTagName();
      List<String> values = t.getValue();
      if ("pubkey".equals(tagName)) {
        return values.contains(takeTag.getMakerPubkey()) || values.contains(takeTag.getTakerPubkey());
      } else if ("nip05".equals(tagName)) {
        return values.contains(takeTag.getMakerNip05()) || values.contains(takeTag.getTakerNip05());
      }
    }
    return false;
  }

  private boolean getBiPredicate(List<GenericTagQuery> anyMatchList,  PostIntentEvent event) {
    if(anyMatchList == null || anyMatchList.isEmpty()){
      return true;
    }
    for(GenericTagQuery t : anyMatchList) {
      String tagName = t.getTagName();
      List<String> values = t.getValue();
        switch (tagName) {
            case "side" -> {
                MakeTag makeTag = event.getSideTag();
                return makeTag != null && values.contains(makeTag.getSide().getSide());
            }
            case "symbol" -> {
                TokenTag token = event.getTokenTag();
                return token != null && values.contains(token.getSymbol());
            }
            case "currency" -> {
                QuoteTag quote = event.getQuoteTag();
                return quote != null && values.contains(quote.getCurrency());
            }
            case "paymentMethod" -> {
                List<PaymentTag> paymentMethods = event.getPaymentTags();
                for (PaymentTag p : paymentMethods) {
                    if (values.contains(p.getMethod())) {
                        return true;
                    }
                }
            }
        }
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
