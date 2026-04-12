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

    public List<DailySummaryResponse> getMonthlyCalendar(UUID portfolioId, int year, int month) {
        List<Trade> allTrades = tradeRepositoryPort.findByPortfolioId(portfolioId);

        return allTrades.stream()
                .filter(trade -> "CLOSED".equals(trade.status()) && trade.exitDate() != null)
                .filter(trade -> trade.exitDate().getYear() == year && trade.exitDate().getMonthValue() == month)
                .collect(Collectors.groupingBy(trade -> trade.exitDate().toLocalDate()))
                .entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Trade> tradesOfThatDay = entry.getValue();
                    
                    int tradeCount = tradesOfThatDay.size();
                    BigDecimal netPnl = tradesOfThatDay.stream()
                            .map(trade -> trade.pnlNet() != null ? trade.pnlNet() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    boolean isProfitable = netPnl.compareTo(BigDecimal.ZERO) > 0;

                    return new DailySummaryResponse(date, tradeCount, netPnl, isProfitable);
                })
                .sorted(Comparator.comparing(DailySummaryResponse::date))
                .toList();
    }

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

    // ==========================================
    // IMPORTACIÓN DE CSV (SOPORTE EXACTO PARA TU MT5)
    // ==========================================
    @Transactional
    public int importTradesFromCsv(UUID portfolioId, org.springframework.web.multipart.MultipartFile file) {
        int importedCount = 0;
        
        Portfolio portfolio = portfolioRepositoryPort.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("El portafolio no existe."));
        
        BigDecimal totalPnlImported = BigDecimal.ZERO;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine || line.trim().isEmpty()) {
                    isFirstLine = false;
                    continue;
                }

                // El "-1" asegura que mantenga las comas finales vacías en el arreglo
                String[] columns = line.replace("\"", "").split(",", -1);
                
                if (columns.length < 14) continue; 

                try {
                    String type = columns[3].trim().toLowerCase(); 
                    if (!type.equals("buy") && !type.equals("sell")) continue;
                    
                    String direction = type.equals("buy") ? "LONG" : "SHORT";
                    BigDecimal positionSize = parseBigDecimalOrZero(columns[4]);
                    String asset = columns[6].trim().toUpperCase();
                    BigDecimal entryPrice = parseBigDecimalOrZero(columns[7]);
                    BigDecimal exitPrice = parseBigDecimalOrZero(columns[8]);
                    
                    BigDecimal stopLoss = parsePriceOrNull(columns[9]);
                    BigDecimal takeProfit = parsePriceOrNull(columns[10]);
                    
                    BigDecimal commission = parseBigDecimalOrZero(columns[11]);
                    BigDecimal swap = parseBigDecimalOrZero(columns[12]);
                    BigDecimal profit = parseBigDecimalOrZero(columns[13]);
                    
                    BigDecimal pnlNet = profit.add(swap).add(commission);
                    
                    // Tu MT5 exporta fechas en formato ISO directo (ej. 2026-04-10T14:11:47)
                    LocalDateTime entryDate = LocalDateTime.parse(columns[1].trim());
                    LocalDateTime exitDate = LocalDateTime.parse(columns[2].trim());

                    Trade newTrade = new Trade(
                            null, portfolioId, null, null, asset, direction, "CLOSED",
                            entryDate, exitDate, entryPrice, exitPrice, positionSize,
                            takeProfit, stopLoss, null, null, null, null,
                            commission, swap, profit, pnlNet, "Importado desde MT5"
                    );

                    tradeRepositoryPort.save(newTrade);
                    totalPnlImported = totalPnlImported.add(pnlNet);
                    importedCount++;

                } catch (Exception e) {
                    log.error("Error parseando línea CSV: {}", line, e);
                }
            }

            if (importedCount > 0) {
                Portfolio updatedPortfolio = new Portfolio(
                        portfolio.id(), portfolio.userId(), portfolio.name(),
                        portfolio.initialBalance(), portfolio.currentBalance().add(totalPnlImported), 
                        portfolio.targetBalance(), portfolio.currency(), portfolio.createdAt()
                );
                portfolioRepositoryPort.save(updatedPortfolio);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al leer el archivo CSV: " + e.getMessage());
        }

        return importedCount;
    }

    // --- MÉTODOS DE SEGURIDAD PARA TEXTO VACÍO ---
    private BigDecimal parseBigDecimalOrZero(String val) {
        if (val == null || val.trim().isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(val.trim()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private BigDecimal parsePriceOrNull(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try { 
            BigDecimal bd = new BigDecimal(val.trim());
            return bd.compareTo(BigDecimal.ZERO) == 0 ? null : bd;
        } catch (Exception e) { return null; }
    }
}