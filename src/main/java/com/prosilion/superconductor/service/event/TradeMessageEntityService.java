package com.prosilion.superconductor.service.event;


import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.entity.TradeMessageEntity;
import com.prosilion.superconductor.repository.TradeMessageEntityRepository;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.Kind;
import nostr.event.impl.TradeMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TradeMessageEntityService implements EventEntityServiceIF<TradeMessageEvent> {

    private final TradeMessageEntityRepository tradeMessageEntityRepository;

    @Autowired
    public TradeMessageEntityService(TradeMessageEntityRepository tradeMessageEntityRepository) {
        this.tradeMessageEntityRepository = tradeMessageEntityRepository;
    }

    @Override
    public Kind getKind() {
        return Kind.TRADE_MESSAGE;
    }

    public Long saveEventEntity(@NonNull TradeMessageEvent event) {
        TradeMessageEntity savedEntity = Optional.of(tradeMessageEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        return savedEntity.getId();
    }

    private TradeMessageEntity populateTradeMessageEntity(TradeMessageEntity tradeMessageEntity) {
        return tradeMessageEntity;
    }

    public Map<Kind, Map<Long, TradeMessageEvent>> getAll() {
        return tradeMessageEntityRepository.findAll().stream()
                .map(this::populateTradeMessageEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(TradeMessageEntity::getId, TradeMessageEntity::convertEntityToDto)));
    }

    @Override
    public TradeMessageEvent getEventById(@NonNull Long id) {
        return populateTradeMessageEntity(tradeMessageEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }
}
