package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.join.ProfileEntityAbstractTagEntity;
import com.prosilion.superconductor.plugin.tag.ProfileTagPlugin;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.join.ProfileEntityAbstractTagEntityRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProfileConcreteTagEntitiesService<
        P extends BaseTag,
        Q extends AbstractTagEntityRepository<R>,
        R extends AbstractTagEntity,
        S extends ProfileEntityAbstractTagEntity,
        T extends ProfileEntityAbstractTagEntityRepository<S>> {
    private final List<ProfileTagPlugin<P, Q, R, S, T>> tagPlugins;

    @Autowired
    public ProfileConcreteTagEntitiesService(List<ProfileTagPlugin<P, Q, R, S, T>> tagPlugins) {
        this.tagPlugins = tagPlugins;
    }


    public List<AbstractTagEntity> getTags(@NonNull Long eventId) {
        return tagPlugins.stream().map(tagModule ->
                        tagModule.getTags(eventId))
                .flatMap(List::stream).distinct().collect(Collectors.toList());
    }

    public void saveTags(@NonNull Long eventId, @NonNull List<P> baseTags) {
        tagPlugins.forEach(module ->
                baseTags.stream().filter(tags ->
                                tags.getCode().equalsIgnoreCase(module.getCode()))
                        .forEach(tag ->
                                module.saveTag(eventId, tag)));
    }
}