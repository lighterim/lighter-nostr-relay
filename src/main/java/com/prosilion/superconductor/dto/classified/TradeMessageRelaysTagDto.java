package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.standard.IntentRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.TradeMessageRelaysTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.RelaysTag;

@Getter
public class TradeMessageRelaysTagDto implements AbstractTagDto {
  private final RelaysTag relaysTag;

  public TradeMessageRelaysTagDto(@NonNull RelaysTag relaysTag) {
    this.relaysTag = relaysTag;
  }

  @Override
  public String getCode() {
    return relaysTag.getCode();
  }

  @Override
  public TradeMessageRelaysTagEntity convertDtoToEntity() {
    return new TradeMessageRelaysTagEntity(relaysTag);
  }
}
