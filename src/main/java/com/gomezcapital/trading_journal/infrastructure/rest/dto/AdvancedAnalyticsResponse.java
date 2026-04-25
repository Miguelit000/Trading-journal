package com.gomezcapital.trading_journal.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdvancedAnalyticsResponse(
    // Métricas Base
    int totalTrades,
    BigDecimal winRate,
    BigDecimal profitFactor,
    
    // Expectancy y RR (En Dólares y en R-Multiples)
    BigDecimal expectancyUsd,
    BigDecimal expectancyR,
    BigDecimal averageRewardToRiskUsd,
    BigDecimal averageRewardToRiskR,
    
    // Métricas de Riesgo (Drawdowns)
    BigDecimal maxDrawdownUsd,
    BigDecimal maxDrawdownPct,
    BigDecimal averageDrawdownUsd,
    int recoveryTimeDays,
    
    // Rachas y Rendimiento
    int maxConsecutiveWins,
    int maxConsecutiveLosses,
    BigDecimal avgHoldingPeriodHours,
    BigDecimal sqn, // System Quality Number
    
    // Excursiones (MAE / MFE) - En % o en Dólares según lo uses
    BigDecimal averageMae,
    BigDecimal averageMfe,
    
    // Datos para el Calendario (Mapa de Calor Anual)
    List<DailySummaryResponse> heatmapData
) {
}