package com.prosilion.superconductor.entity.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.QuoteTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.tag.QuoteTag;

import java.math.BigDecimal;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "quote_tag")
public class QuoteTagEntity extends AbstractTagEntity {

    private BigDecimal number;
    private String currency;
    private BigDecimal usdRate;

    public QuoteTagEntity(@NonNull QuoteTag quoteTag){
        this.number = quoteTag.getNumber();
        this.currency = quoteTag.getCurrency();
        this.usdRate = quoteTag.getUsdRate();
    }

    @Override
    public String getCode() {
        return "quote";
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new QuoteTagDto(new QuoteTag(number, currency, usdRate));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new QuoteTag(number, currency, usdRate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuoteTagEntity that = (QuoteTagEntity) o;
        return Objects.equals(number, that.number) && Objects.equals(currency, that.currency) && Objects.equals(usdRate, that.usdRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, currency, usdRate);
    }
}
