package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.LimitTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.tag.LimitTag;

import java.math.BigDecimal;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "limit_tag")
public class LimitTagEntity extends AbstractTagEntity {

    private String currency;
    private BigDecimal lowLimit;
    private BigDecimal upLimit;


    public LimitTagEntity(@NonNull LimitTag limitTag){
        this.currency = limitTag.getCurrency();
        this.lowLimit = limitTag.getLowLimit();
        this.upLimit = limitTag.getUpLimit();
    }

    @Override
    public String getCode() {
        return "limit";
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new LimitTagDto(new LimitTag(currency, lowLimit, upLimit));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new LimitTag(currency, lowLimit, upLimit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LimitTagEntity that = (LimitTagEntity) o;
        return Objects.equals(currency, that.currency) && Objects.equals(lowLimit, that.lowLimit) && Objects.equals(upLimit, that.upLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, lowLimit, upLimit);
    }
}
