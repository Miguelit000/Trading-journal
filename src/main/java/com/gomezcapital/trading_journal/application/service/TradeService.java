package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.domain.model.TradeImage;
import com.gomezcapital.trading_journal.domain.ports.TradeRepositoryPort;
import com.gomezcapital.trading_journal.domain.ports.TradeImageRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.UpdateTradeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepositoryPort tradeRepositoryPort;
    private final TradeImageRepositoryPort tradeImageRepositoryPort; // <-- NUEVO: Acceso a la galería

    public Trade logNewTrade(Trade trade) {
        validateNewTrade(trade);
        Trade tradeToSave = new Trade(
                trade.id(), trade.portfolioId(), trade.strategyId(), trade.playbookId(),
                trade.asset(), trade.direction(), "OPEN",
                trade.entryDate() != null ? trade.entryDate() : LocalDateTime.now(),
                null, trade.entryPrice(), null, trade.positionSize(),
                trade.takeProfit(), trade.stopLoss(), defaultToZero(trade.plannedRr()),
                defaultToZero(trade.actualRr()), null, null,
                defaultToZero(trade.commissions()), defaultToZero(trade.feesAndSwaps()),
                BigDecimal.ZERO, BigDecimal.ZERO, trade.notes()
        );
        return tradeRepositoryPort.save(tradeToSave);
    }

    public List<Trade> getTradesByPortfolioId(UUID portfolioId) {
        return tradeRepositoryPort.findByPortfolioId(portfolioId);
    }

    public Trade closeTrade(UUID tradeId, BigDecimal exitPrice, LocalDateTime exitDate, BigDecimal pnlNet) {
        Trade existingTrade = tradeRepositoryPort.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("El trade no existe."));

        if ("CLOSED".equals(existingTrade.status())) {
            throw new IllegalStateException("El trade ya se encuentra cerrado.");
        }

        BigDecimal finalPnl = pnlNet != null ? pnlNet : BigDecimal.ZERO;

        Trade closedTrade = new Trade(
                existingTrade.id(), existingTrade.portfolioId(), existingTrade.strategyId(), existingTrade.playbookId(),
                existingTrade.asset(), existingTrade.direction(), "CLOSED",
                existingTrade.entryDate(), exitDate, existingTrade.entryPrice(), exitPrice,
                existingTrade.positionSize(), existingTrade.takeProfit(), existingTrade.stopLoss(),
                existingTrade.plannedRr(), existingTrade.actualRr(), existingTrade.mfePrice(), existingTrade.maePrice(), 
                existingTrade.commissions(), existingTrade.feesAndSwaps(), finalPnl, finalPnl, existingTrade.notes()
        );

        return tradeRepositoryPort.save(closedTrade);
    }

    public Trade updateTrade(UUID tradeId, UpdateTradeRequest request) {
        Trade existingTrade = tradeRepositoryPort.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("El trade no existe."));

        Trade updatedTrade = new Trade(
                existingTrade.id(), existingTrade.portfolioId(), request.strategyId(), existingTrade.playbookId(),
                request.asset().toUpperCase(), request.direction(), existingTrade.status(),
                existingTrade.entryDate(), existingTrade.exitDate(), request.entryPrice(), request.exitPrice(),
                request.positionSize(), request.takeProfit(), request.stopLoss(),
                existingTrade.plannedRr(), existingTrade.actualRr(), existingTrade.mfePrice(), existingTrade.maePrice(), 
                existingTrade.commissions(), existingTrade.feesAndSwaps(), 
                request.pnlNet() != null ? request.pnlNet() : existingTrade.pnlGross(),
                request.pnlNet() != null ? request.pnlNet() : existingTrade.pnlNet(), request.notes()
        );

        return tradeRepositoryPort.save(updatedTrade);
    }

    // ==========================================
    // NUEVA LÓGICA DE GALERÍA (4FN)
    // ==========================================
    public void addImageToTrade(UUID tradeId, String fileName) {
        TradeImage newImage = new TradeImage(null, tradeId, fileName, LocalDateTime.now());
        tradeImageRepositoryPort.save(newImage);
    }

    public List<String> getTradeImages(UUID tradeId) {
        return tradeImageRepositoryPort.findByTradeId(tradeId)
                .stream().map(TradeImage::fileName).toList();
    }

    private void validateNewTrade(Trade trade) {
        if (trade.entryPrice() == null || trade.entryPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de entrada debe ser mayor a 0.");
        }
    }

    private BigDecimal defaultToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}