package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.domain.model.TradeMetrics;
import com.gomezcapital.trading_journal.domain.ports.TradeRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TradeRepositoryPort tradeRepositoryPort;

    public TradeMetrics calculateAccountMetrics(UUID portfolioId) {
        log.info("Calculando metricas para el dashboard del portafolio: {}", portfolioId);

        // <-- Llama al nuevo método del repositorio usando portfolioId
        List<Trade> allTrades = tradeRepositoryPort.findByPortfolioId(portfolioId);

        List<Trade> closedTrades = allTrades.stream()
                .filter(trade -> "CLOSED".equals(trade.status()))
                .toList();

        if (closedTrades.isEmpty()) {
            return new TradeMetrics(0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }    
        
        int totalTrades = closedTrades.size();
        int winningTrades = 0;
        int losingTrades = 0;
        BigDecimal grossProfit = BigDecimal.ZERO;
        BigDecimal grossLoss = BigDecimal.ZERO;
        BigDecimal totalPnl = BigDecimal.ZERO;

        for (Trade trade : closedTrades) {
            BigDecimal pnl = trade.pnlNet() != null ? trade.pnlNet() : BigDecimal.ZERO;
            totalPnl = totalPnl.add(pnl);

            if (pnl.compareTo(BigDecimal.ZERO) > 0) {
                winningTrades++;
                grossProfit = grossProfit.add(pnl);
            } else if (pnl.compareTo(BigDecimal.ZERO) < 0) {
                losingTrades++;
                grossLoss = grossLoss.add(pnl.abs());
            }
        }

        BigDecimal winRate = BigDecimal.valueOf((double) winningTrades / totalTrades * 100)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal profitFactor;
        if (grossLoss.compareTo(BigDecimal.ZERO) == 0) {
            profitFactor = grossProfit; 
        } else {
            profitFactor = grossProfit.divide(grossLoss, 2, RoundingMode.HALF_UP);
        }

        return new TradeMetrics(
            totalTrades, winningTrades, losingTrades,
            winRate, profitFactor, totalPnl
        );
    }
}