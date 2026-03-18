package com.gomezcapital.trading_journal.infrastructure.persistence.adapter;

import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.domain.ports.TradeRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.AccountEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.TradeEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.TradeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component // Crea una instancia y la mantiene lista
@RequiredArgsConstructor // Inyecta el JpaRepository automáticamente
public class TradeRepositoryAdapter implements TradeRepositoryPort {

    private final TradeJpaRepository tradeJpaRepository;

    @Override
    public Trade save(Trade trade) {
        // Traducir de Dominio (Record) a Infraestructura (Entity)
        TradeEntity entity = toEntity(trade);
        
        // Guardar en PostgreSQL usando Spring Data JPA
        TradeEntity savedEntity = tradeJpaRepository.save(entity);
        
        // Traducir de vuelta al Dominio y retornarlo
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Trade> findById(UUID id) {
        return tradeJpaRepository.findById(id)
                .map(this::toDomain); // Si lo encuentra, lo traduce
    }

    @Override
    public List<Trade> findByAccountId(UUID accountId) {
        return tradeJpaRepository.findByAccount_Id(accountId).stream()
                .map(this::toDomain) // Traduce la lista entera
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        tradeJpaRepository.deleteById(id);
    }

    // ==========================================
    // MÉTODOS PRIVADOS DE TRADUCCIÓN (MAPPERS)
    // ==========================================

    private TradeEntity toEntity(Trade trade) {
        if (trade == null) return null;

        AccountEntity accountReference = null;
        if (trade.accountId() != null) {
            accountReference = new AccountEntity();
            accountReference.setId(trade.accountId());
        }
        
        // Usamos el patrón Builder de Lombok que le pusimos a TradeEntity
        return TradeEntity.builder()
                .id(trade.id())
                .account(accountReference) 
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
                .build();
    }

    private Trade toDomain(TradeEntity entity) {
        if (entity == null) return null;

        // Creamos el record 
        return new Trade(
                entity.getId(),
                entity.getAccount() != null ? entity.getAccount().getId() : null,
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
                null, // plannedRr
                null, // actualRr
                null, // mfePrice
                null, // maePrice
                null, // commissions
                null, // feesAndSwaps
                entity.getPnlGross(),
                entity.getPnlNet(),
                entity.getNotes()
        );
    }
}