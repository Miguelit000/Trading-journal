package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.domain.model.TradeImage;
import com.gomezcapital.trading_journal.domain.ports.PortfolioRepositoryPort;
import com.gomezcapital.trading_journal.domain.ports.TradeRepositoryPort;
import com.gomezcapital.trading_journal.domain.ports.TradeImageRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.DailySummaryResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.UpdateTradeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepositoryPort tradeRepositoryPort;
    private final TradeImageRepositoryPort tradeImageRepositoryPort;
    private final PortfolioRepositoryPort portfolioRepositoryPort;

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

    @Transactional
    public Trade closeTrade(UUID tradeId, BigDecimal exitPrice, LocalDateTime exitDate, BigDecimal pnlNet) {
        Trade existingTrade = tradeRepositoryPort.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("El trade no existe."));

        if ("CLOSED".equals(existingTrade.status())) {
            throw new IllegalStateException("El trade ya se encuentra cerrado.");
        }

        BigDecimal finalPnl = pnlNet != null ? pnlNet : BigDecimal.ZERO;

        Portfolio portfolio = portfolioRepositoryPort.findById(existingTrade.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("El portfolio asociado no existe."));

        BigDecimal newBalance = portfolio.currentBalance().add(finalPnl);
        Portfolio updatedPortfolio = new Portfolio(
                portfolio.id(), portfolio.userId(), portfolio.name(),
                portfolio.initialBalance(), newBalance, portfolio.targetBalance(),
                portfolio.currency(), portfolio.createdAt()
        );
        portfolioRepositoryPort.save(updatedPortfolio);

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
    // NUEVO: LÓGICA DE CALENDARIO (Agrupación)
    // ==========================================
    public List<DailySummaryResponse> getMonthlyCalendar(UUID portfolioId, int year, int month) {
        List<Trade> allTrades = tradeRepositoryPort.findByPortfolioId(portfolioId);

        return allTrades.stream()
                // 1. Filtramos solo los trades cerrados que tengan fecha de salida
                .filter(trade -> "CLOSED".equals(trade.status()) && trade.exitDate() != null)
                // 2. Filtramos que correspondan al mes y año solicitado
                .filter(trade -> trade.exitDate().getYear() == year && trade.exitDate().getMonthValue() == month)
                // 3. Agrupamos por el día exacto (LocalDate)
                .collect(Collectors.groupingBy(trade -> trade.exitDate().toLocalDate()))
                .entrySet().stream()
                // 4. Transformamos cada grupo en nuestro DTO DailySummaryResponse
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Trade> tradesOfThatDay = entry.getValue();
                    
                    int tradeCount = tradesOfThatDay.size();
                    
                    // Sumamos el PnL Neto de todas las operaciones del día
                    BigDecimal netPnl = tradesOfThatDay.stream()
                            .map(trade -> trade.pnlNet() != null ? trade.pnlNet() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // Determinamos si el día fue en verde o en rojo (mayor a 0 es ganancia)
                    boolean isProfitable = netPnl.compareTo(BigDecimal.ZERO) > 0;

                    return new DailySummaryResponse(date, tradeCount, netPnl, isProfitable);
                })
                // 5. Ordenamos por fecha ascendente para que React lo reciba ordenado
                .sorted(Comparator.comparing(DailySummaryResponse::date))
                .toList();
    }

    // ==========================================
    // LÓGICA DE GALERÍA (4FN)
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