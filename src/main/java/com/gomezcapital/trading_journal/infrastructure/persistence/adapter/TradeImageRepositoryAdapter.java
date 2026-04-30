package com.gomezcapital.trading_journal.infrastructure.persistence.adapter;

import com.gomezcapital.trading_journal.domain.model.TradeImage;
import com.gomezcapital.trading_journal.domain.ports.TradeImageRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.TradeImageEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.TradeImageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TradeImageRepositoryAdapter implements TradeImageRepositoryPort {

    private final TradeImageJpaRepository repository;

    @Override
    public TradeImage save(TradeImage tradeImage) {
        TradeImageEntity entity = TradeImageEntity.builder()
                .id(tradeImage.id())
                .tradeId(tradeImage.tradeId())
                .fileName(tradeImage.fileName())
                .createdAt(tradeImage.createdAt())
                .build();
                
        TradeImageEntity savedEntity = repository.save(entity);
        
        return new TradeImage(
                savedEntity.getId(),
                savedEntity.getTradeId(),
                savedEntity.getFileName(),
                savedEntity.getCreatedAt()
        );
    }

    @Override
    public List<TradeImage> findByTradeId(UUID tradeId) {
        return repository.findByTradeId(tradeId).stream()
                .map(entity -> new TradeImage(
                        entity.getId(),
                        entity.getTradeId(),
                        entity.getFileName(),
                        entity.getCreatedAt()
                )).toList();
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}