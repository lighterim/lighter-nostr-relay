package com.prosilion.superconductor.entity.join.subscriber;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.event.impl.Filters;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Component
public class GenericFiltersFilter extends AbstractFilterType{
    private Filters filters;

    public GenericFiltersFilter(Long filterId, Filters filters) {
        super(filterId);
        this.filters = filters;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        GenericFiltersFilter that = (GenericFiltersFilter) o;
        return Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(filters);
    }

    @Override
    public String getCode() {
        return "filters";
    }
}
