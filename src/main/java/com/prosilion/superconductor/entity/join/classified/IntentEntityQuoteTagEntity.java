package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "intent_quote_tag_join")
public class IntentEntityQuoteTagEntity extends EventEntityAbstractTagEntity {

    private Long quoteTagId;

    public <T extends EventEntityAbstractTagEntity> IntentEntityQuoteTagEntity(Long eventId, Long quoteTagId){
        super.setEventId(eventId);
        this.quoteTagId = quoteTagId;
    }

}
