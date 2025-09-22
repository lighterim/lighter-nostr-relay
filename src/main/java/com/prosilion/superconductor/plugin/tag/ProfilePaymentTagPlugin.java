package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.ProfilePaymentTagDto;
import com.prosilion.superconductor.entity.join.classified.ProfileEntityPaymentTagEntity;
import com.prosilion.superconductor.entity.standard.ProfilePaymentTagEntity;
import com.prosilion.superconductor.repository.classified.ProfilePaymentTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.ProfileEntityPaymentTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.PaymentTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfilePaymentTagPlugin<
        P extends PaymentTag,
        Q extends ProfilePaymentTagEntityRepository<R>,
        R extends ProfilePaymentTagEntity,
        S extends ProfileEntityPaymentTagEntity,
        T extends ProfileEntityPaymentTagEntityRepository<S>>
        implements ProfileTagPlugin<P, Q, R, S, T> {

    private final ProfilePaymentTagEntityRepository<R> relaysTagEntityRepository;
    private final ProfileEntityPaymentTagEntityRepository<S> join;

    @Autowired
    public ProfilePaymentTagPlugin(@Nonnull ProfilePaymentTagEntityRepository<R> intentRelaysTagEntityRepository, @NonNull ProfileEntityPaymentTagEntityRepository<S> join) {
        this.relaysTagEntityRepository = intentRelaysTagEntityRepository;
        this.join = join;
    }

    @Override
    public String getCode() {
        return "relays";
    }

    @Override
    public R convertDtoToEntity(P relaysTag) {
        return (R) getTagDto(relaysTag).convertDtoToEntity();
    }

    @Override
    public ProfilePaymentTagDto getTagDto(P relaysTag) {
        return new ProfilePaymentTagDto(relaysTag);
    }

    @Override
    public S getEventEntityTagEntity(Long eventId, Long relaysTagId) {
        return (S) new ProfileEntityPaymentTagEntity(eventId, relaysTagId);
    }

    @Override
    public T getEventEntityStandardTagEntityRepositoryJoin() {
        return (T) join;
    }

    @Override
    public Q getStandardTagEntityRepository() {
        return (Q) relaysTagEntityRepository;
    }
}
