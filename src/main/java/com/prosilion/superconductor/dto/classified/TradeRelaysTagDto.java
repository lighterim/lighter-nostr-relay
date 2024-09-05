package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.standard.IntentRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.TradeRelaysTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.RelaysTag;

@Getter
public class TradeRelaysTagDto implements AbstractTagDto {
  private final RelaysTag relaysTag;

  public TradeRelaysTagDto(@NonNull RelaysTag relaysTag) {
    this.relaysTag = relaysTag;
  }

  @Override
  public String getCode() {
    return relaysTag.getCode();
  }

  @Override
  public TradeRelaysTagEntity convertDtoToEntity() {
    return new TradeRelaysTagEntity(relaysTag);
  }
}
