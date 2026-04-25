package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.Portfolio;
import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.domain.ports.PortfolioRepositoryPort;
import com.gomezcapital.trading_journal.domain.ports.TradeRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.AdvancedAnalyticsResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.DailySummaryResponse;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.TradeMetricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TradeRepositoryPort tradeRepositoryPort;
    private final PortfolioRepositoryPort portfolioRepositoryPort;

    // =========================================================
    // MÉTODO ORIGINAL RESTAURADO (Para el Dashboard)
    // =========================================================
    public TradeMetricsResponse calculateMetrics(UUID portfolioId) {
        List<Trade> closedTrades = tradeRepositoryPort.findByPortfolioId(portfolioId).stream()
                .filter(t -> "CLOSED".equals(t.status()))
                .toList();

        if (closedTrades.isEmpty()) {
            return new TradeMetricsResponse(0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        int totalTrades = closedTrades.size();
        List<Trade> wins = closedTrades.stream().filter(t -> t.pnlNet() != null && t.pnlNet().compareTo(BigDecimal.ZERO) > 0).toList();
        List<Trade> losses = closedTrades.stream().filter(t -> t.pnlNet() != null && t.pnlNet().compareTo(BigDecimal.ZERO) < 0).toList();

        BigDecimal winRate = BigDecimal.valueOf(wins.size())
                .divide(BigDecimal.valueOf(totalTrades), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal grossProfit = wins.stream().map(Trade::pnlNet).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grossLoss = losses.stream().map(Trade::pnlNet).reduce(BigDecimal.ZERO, BigDecimal::add).abs();

        BigDecimal profitFactor = grossLoss.compareTo(BigDecimal.ZERO) == 0 ? grossProfit : grossProfit.divide(grossLoss, 2, RoundingMode.HALF_UP);
        BigDecimal totalPnl = grossProfit.subtract(grossLoss);

        return new TradeMetricsResponse(totalTrades, wins.size(), losses.size(), winRate, profitFactor, totalPnl);
    }

    // =========================================================
    // MÉTODO NUEVO (Para la Pestaña de Estadísticas Avanzadas)
    // =========================================================
    public AdvancedAnalyticsResponse calculateAdvancedAnalytics(UUID portfolioId) {
        Portfolio portfolio = portfolioRepositoryPort.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portafolio no encontrado"));

        List<Trade> allTrades = tradeRepositoryPort.findByPortfolioId(portfolioId);
        
        List<Trade> closedTrades = allTrades.stream()
                .filter(t -> "CLOSED".equals(t.status()) && t.exitDate() != null)
                .sorted(Comparator.comparing(Trade::exitDate))
                .toList();

        if (closedTrades.isEmpty()) {
            return emptyResponse();
        }

        int totalTrades = closedTrades.size();
        List<Trade> wins = closedTrades.stream().filter(t -> t.pnlNet() != null && t.pnlNet().compareTo(BigDecimal.ZERO) > 0).toList();
        List<Trade> losses = closedTrades.stream().filter(t -> t.pnlNet() != null && t.pnlNet().compareTo(BigDecimal.ZERO) < 0).toList();

        BigDecimal winRate = BigDecimal.valueOf(wins.size())
                .divide(BigDecimal.valueOf(totalTrades), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal grossProfit = wins.stream().map(Trade::pnlNet).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grossLoss = losses.stream().map(Trade::pnlNet).reduce(BigDecimal.ZERO, BigDecimal::add).abs();

        BigDecimal profitFactor = grossLoss.compareTo(BigDecimal.ZERO) == 0 ? grossProfit : grossProfit.divide(grossLoss, 2, RoundingMode.HALF_UP);

        BigDecimal netPnl = grossProfit.subtract(grossLoss);
        BigDecimal expectancyUsd = netPnl.divide(BigDecimal.valueOf(totalTrades), 2, RoundingMode.HALF_UP);

        BigDecimal avgWin = wins.isEmpty() ? BigDecimal.ZERO : grossProfit.divide(BigDecimal.valueOf(wins.size()), 2, RoundingMode.HALF_UP);
        BigDecimal avgLoss = losses.isEmpty() ? BigDecimal.ZERO : grossLoss.divide(BigDecimal.valueOf(losses.size()), 2, RoundingMode.HALF_UP);
        BigDecimal averageRewardToRiskUsd = avgLoss.compareTo(BigDecimal.ZERO) == 0 ? avgWin : avgWin.divide(avgLoss, 2, RoundingMode.HALF_UP);

        BigDecimal totalR = closedTrades.stream()
                .map(t -> t.actualRr() != null ? t.actualRr() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectancyR = totalR.divide(BigDecimal.valueOf(totalTrades), 2, RoundingMode.HALF_UP);
        
        BigDecimal peak = portfolio.initialBalance() != null ? portfolio.initialBalance() : BigDecimal.ZERO;
        BigDecimal currentBalance = peak;
        BigDecimal maxDrawdownUsd = BigDecimal.ZERO;
        BigDecimal maxDrawdownPct = BigDecimal.ZERO;
        BigDecimal totalDrawdownUsd = BigDecimal.ZERO;
        int drawdownDays = 0;

        int currentWinStreak = 0, maxWinStreak = 0;
        int currentLossStreak = 0, maxLossStreak = 0;

        long totalDurationSeconds = 0;
        BigDecimal totalMae = BigDecimal.ZERO;
        BigDecimal totalMfe = BigDecimal.ZERO;
        int maeCount = 0, mfeCount = 0;

        for (Trade t : closedTrades) {
            BigDecimal pnl = t.pnlNet() != null ? t.pnlNet() : BigDecimal.ZERO;
            
            if (pnl.compareTo(BigDecimal.ZERO) > 0) {
                currentWinStreak++; maxWinStreak = Math.max(maxWinStreak, currentWinStreak); currentLossStreak = 0;
            } else if (pnl.compareTo(BigDecimal.ZERO) < 0) {
                currentLossStreak++; maxLossStreak = Math.max(maxLossStreak, currentLossStreak); currentWinStreak = 0;
            } else {
                currentWinStreak = 0; currentLossStreak = 0;
            }

            currentBalance = currentBalance.add(pnl);
            if (currentBalance.compareTo(peak) > 0) {
                peak = currentBalance;
            } else {
                BigDecimal ddUsd = peak.subtract(currentBalance);
                BigDecimal ddPct = peak.compareTo(BigDecimal.ZERO) > 0 ? ddUsd.divide(peak, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
                
                if (ddUsd.compareTo(maxDrawdownUsd) > 0) maxDrawdownUsd = ddUsd;
                if (ddPct.compareTo(maxDrawdownPct) > 0) maxDrawdownPct = ddPct;
                totalDrawdownUsd = totalDrawdownUsd.add(ddUsd);
                drawdownDays++;
            }

            if (t.entryDate() != null && t.exitDate() != null) {
                totalDurationSeconds += Duration.between(t.entryDate(), t.exitDate()).getSeconds();
            }

            if (t.maePrice() != null) { totalMae = totalMae.add(t.maePrice()); maeCount++; }
            if (t.mfePrice() != null) { totalMfe = totalMfe.add(t.mfePrice()); mfeCount++; }
        }

        BigDecimal avgDrawdownUsd = drawdownDays == 0 ? BigDecimal.ZERO : totalDrawdownUsd.divide(BigDecimal.valueOf(drawdownDays), 2, RoundingMode.HALF_UP);
        BigDecimal avgHoldingHours = BigDecimal.valueOf(totalDurationSeconds).divide(BigDecimal.valueOf(3600 * totalTrades), 2, RoundingMode.HALF_UP);
        BigDecimal avgMae = maeCount == 0 ? BigDecimal.ZERO : totalMae.divide(BigDecimal.valueOf(maeCount), 2, RoundingMode.HALF_UP);
        BigDecimal avgMfe = mfeCount == 0 ? BigDecimal.ZERO : totalMfe.divide(BigDecimal.valueOf(mfeCount), 2, RoundingMode.HALF_UP);

        double mean = expectancyUsd.doubleValue();
        double sumSq = 0;
        for (Trade t : closedTrades) {
            double diff = (t.pnlNet() != null ? t.pnlNet().doubleValue() : 0) - mean;
            sumSq += diff * diff;
        }
        double stdDev = Math.sqrt(sumSq / totalTrades);
        double sqnValue = stdDev == 0 ? 0 : (mean / stdDev) * Math.sqrt(totalTrades);
        BigDecimal sqn = BigDecimal.valueOf(sqnValue).setScale(2, RoundingMode.HALF_UP);

        List<DailySummaryResponse> heatmap = closedTrades.stream()
            .collect(Collectors.groupingBy(t -> t.exitDate().toLocalDate()))
            .entrySet().stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                List<Trade> tradesOfThatDay = entry.getValue();
                BigDecimal dayPnl = tradesOfThatDay.stream()
                        .map(t -> t.pnlNet() != null ? t.pnlNet() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new DailySummaryResponse(date, tradesOfThatDay.size(), dayPnl, dayPnl.compareTo(BigDecimal.ZERO) > 0);
            })
            .sorted(Comparator.comparing(DailySummaryResponse::date))
            .toList();

        return new AdvancedAnalyticsResponse(
                totalTrades, winRate, profitFactor,
                expectancyUsd, expectancyR, averageRewardToRiskUsd, BigDecimal.ZERO, 
                maxDrawdownUsd, maxDrawdownPct, avgDrawdownUsd, 0, 
                maxWinStreak, maxLossStreak, avgHoldingHours, sqn,
                avgMae, avgMfe, heatmap
        );
    }

    private AdvancedAnalyticsResponse emptyResponse() {
        return new AdvancedAnalyticsResponse(
                0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, new ArrayList<>()
        );
    }
}