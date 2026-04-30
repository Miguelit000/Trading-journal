package com.gomezcapital.trading_journal.infrastructure.persistence.adapter;

import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.domain.ports.TradeRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.PortfolioEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.TradeEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.TradeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TradeRepositoryAdapter implements TradeRepositoryPort {

    private final TradeJpaRepository tradeJpaRepository;

    @Override
    public Trade save(Trade trade) {
        TradeEntity entity = toEntity(trade);
        TradeEntity savedEntity = tradeJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Trade> findById(UUID id) {
        return tradeJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Trade> findByPortfolioId(UUID portfolioId) { // <-- CAMBIO
        return tradeJpaRepository.findByPortfolioId(portfolioId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        tradeJpaRepository.deleteById(id);
    }

    private TradeEntity toEntity(Trade trade) {
        if (trade == null) return null;

        PortfolioEntity portfolioRef = null;
        if (trade.portfolioId() != null) {
            portfolioRef = new PortfolioEntity();
            portfolioRef.setId(trade.portfolioId());
        }
        
        return TradeEntity.builder()
                .id(trade.id())
                .portfolio(portfolioRef) // <-- CAMBIO
                .strategyId(trade.strategyId())
                .playbookId(trade.playbookId())
                .asset(trade.asset())
                .direction(trade.direction())
                .status(trade.status())
                .entryDate(trade.entryDate())
                .exitDate(trade.exitDate())
                .entryPrice(trade.entryPrice())
                .exitPrice(trade.exitPrice())
                .positionSize(trade.positionSize())
                .takeProfit(trade.takeProfit())
                .stopLoss(trade.stopLoss())
                .pnlGross(trade.pnlGross())
                .pnlNet(trade.pnlNet())
                .notes(trade.notes())
                // ELIMINADO imageName
                .build();
    }

    private Trade toDomain(TradeEntity entity) {
        if (entity == null) return null;

        return new Trade(
                entity.getId(),
                entity.getPortfolio() != null ? entity.getPortfolio().getId() : null, // <-- CAMBIO
                entity.getStrategyId(),
                entity.getPlaybookId(),
                entity.getAsset(),
                entity.getDirection(),
                entity.getStatus(),
                entity.getEntryDate(),
                entity.getExitDate(),
                entity.getEntryPrice(),
                entity.getExitPrice(),
                entity.getPositionSize(),
                entity.getTakeProfit(),
                entity.getStopLoss(),
                entity.getPlannedRr(), 
                entity.getActualRr(), 
                entity.getMfePrice(), 
                entity.getMaePrice(), 
                entity.getCommissions(), 
                entity.getFeesAndSwaps(),
                entity.getPnlGross(),
                entity.getPnlNet(),
                entity.getNotes()
                // ELIMINADO imageName
        );
    }
}