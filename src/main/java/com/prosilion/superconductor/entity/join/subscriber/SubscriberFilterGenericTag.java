package com.prosilion.superconductor.entity.join.subscriber;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.GenericTagQuery;
import nostr.event.impl.GenericTag;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Component
public class SubscriberFilterGenericTag extends AbstractFilterType {

    private GenericTagQuery genericTagQuery;

    public SubscriberFilterGenericTag(Long filterId, GenericTagQuery genericTagQuery){
        super(filterId);
        this.genericTagQuery = genericTagQuery;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericTagQuery that = (GenericTagQuery) o;
        return Objects.equals(genericTagQuery.getTagName(), that.getTagName()) &&
                Objects.equals(genericTagQuery.getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        if (genericTagQuery == null) {
            return Objects.hashCode(null);
        }else {
            return Objects.hash(genericTagQuery.getTagName(), genericTagQuery.getValue());
        }
    }

    @Override
    public String getCode() {
        return "genericTag";
    }
}
