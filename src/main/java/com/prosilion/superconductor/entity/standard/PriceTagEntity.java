package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.PriceTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.tag.PriceTag;

import java.math.BigDecimal;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "price_tag")
public class PriceTagEntity extends AbstractTagEntity {
  private BigDecimal number;
  private String currency;
  private String frequency;

  public PriceTagEntity(@NonNull PriceTag priceTag) {
    this.number = priceTag.getNumber();
    this.currency = priceTag.getCurrency();
    this.frequency = priceTag.getFrequency();
  }

  @Override
  public String getCode() {
    return "price";
  }

  @Override
  public BaseTag getAsBaseTag() {
    return new PriceTag(number, currency, frequency);
  }

  @Override
  public AbstractTagDto convertEntityToDto() {
    return new PriceTagDto(new PriceTag(number, currency, frequency));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PriceTagEntity that = (PriceTagEntity) o;
    return Objects.equals(number, that.number) && Objects.equals(currency, that.currency) && Objects.equals(frequency, that.frequency);
  }
  @Override
  public int hashCode() {
    return Objects.hash(number, currency, frequency);
  }
}
