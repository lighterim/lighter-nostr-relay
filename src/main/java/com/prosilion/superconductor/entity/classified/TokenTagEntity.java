package com.prosilion.superconductor.entity.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.TokenTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.tag.TokenTag;

import java.math.BigDecimal;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name="token_tag")
public class TokenTagEntity extends AbstractTagEntity {

    private BigDecimal amount;
    private String symbol;
    private String chain;
    private String network;
    private String address;


    public TokenTagEntity(@NonNull TokenTag tokenTag) {
        this.amount = tokenTag.getAmount();
        this.symbol = tokenTag.getSymbol();
        this.chain = tokenTag.getChain();
        this.network = tokenTag.getNetwork();
        this.address = tokenTag.getAddress();
    }

    @Override
    public String getCode() {
        return "token";
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new TokenTagDto(new TokenTag(symbol, chain, network, address, amount));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new TokenTag(symbol, chain, network, address, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenTagEntity that = (TokenTagEntity) o;
        return Objects.equals(amount, that.amount) && Objects.equals(symbol, that.symbol) && Objects.equals(chain, that.chain) && Objects.equals(network, that.network) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, symbol, chain, network, address);
    }
}
