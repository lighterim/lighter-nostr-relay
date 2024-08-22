package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "event-quote_tag-join")
public class EventEntityQuoteTagEntity extends EventEntityAbstractTagEntity {

    private Long quoteTagId;

    public <T extends EventEntityAbstractTagEntity> EventEntityQuoteTagEntity(Long eventId, Long quoteTagId){
        super.setEventId(eventId);
        this.quoteTagId = quoteTagId;
    }

}
