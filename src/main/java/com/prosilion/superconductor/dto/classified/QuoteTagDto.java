package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.standard.QuoteTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.QuoteTag;

@Getter
public class QuoteTagDto implements AbstractTagDto {

    private final QuoteTag quoteTag;

    public QuoteTagDto(@NonNull QuoteTag quoteTag){
        this.quoteTag = quoteTag;
    }

    @Override
    public String getCode() {
        return quoteTag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new QuoteTagEntity(quoteTag);
    }
}
