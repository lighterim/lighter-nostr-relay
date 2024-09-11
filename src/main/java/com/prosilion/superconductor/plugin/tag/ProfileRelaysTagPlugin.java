package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.ProfileRelaysTagDto;
import com.prosilion.superconductor.entity.join.classified.ProfileEntityRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.ProfileRelaysTagEntity;
import com.prosilion.superconductor.repository.join.standard.ProfileEntityRelaysTagEntityRepository;
import com.prosilion.superconductor.repository.standard.ProfileRelaysTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.RelaysTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileRelaysTagPlugin<
        P extends RelaysTag,
        Q extends ProfileRelaysTagEntityRepository<R>,
        R extends ProfileRelaysTagEntity,
        S extends ProfileEntityRelaysTagEntity,
        T extends ProfileEntityRelaysTagEntityRepository<S>>
        implements ProfileTagPlugin<P, Q, R, S, T> {

    private final ProfileRelaysTagEntityRepository<R> relaysTagEntityRepository;
    private final ProfileEntityRelaysTagEntityRepository<S> join;

    @Autowired
    public ProfileRelaysTagPlugin(@Nonnull ProfileRelaysTagEntityRepository<R> intentRelaysTagEntityRepository, @NonNull ProfileEntityRelaysTagEntityRepository<S> join) {
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
    public ProfileRelaysTagDto getTagDto(P relaysTag) {
        return new ProfileRelaysTagDto(relaysTag);
    }

    @Override
    public S getEventEntityTagEntity(Long eventId, Long relaysTagId) {
        return (S) new ProfileEntityRelaysTagEntity(eventId, relaysTagId);
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
