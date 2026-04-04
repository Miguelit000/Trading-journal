package com.gomezcapital.trading_journal.application.service;

import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.domain.ports.TradeRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.CloseTradeRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.TradeResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * Registra un nuevo Trade en el sistema.
     */
    public Trade logNewTrade(Trade trade) {
        log.info("Intentando registrar un nuevo trade para la cuenta: {}", trade.accountId());

        validateNewTrade(trade);

        // BLINDAJE: Usamos defaultToZero para garantizar que nunca haya un NullPointerException
        // al momento de hacer cálculos financieros en el futuro.
        Trade tradeToSave = new Trade(
                trade.id(), 
                trade.accountId(), 
                trade.strategyId(), 
                trade.playbookId(),
                trade.asset(), 
                trade.direction(), 
                "OPEN", // Forzamos el estado a OPEN
                trade.entryDate() != null ? trade.entryDate() : LocalDateTime.now(),
                null, // exitDate
                trade.entryPrice(), 
                null, // exitPrice
                trade.positionSize(),
                trade.takeProfit(), 
                trade.stopLoss(), 
                defaultToZero(trade.plannedRr()),
                defaultToZero(trade.actualRr()), 
                null, // mfePrice
                null, // maePrice
                defaultToZero(trade.commissions()), 
                defaultToZero(trade.feesAndSwaps()),
                BigDecimal.ZERO, // pnlGross al abrir siempre es 0
                BigDecimal.ZERO, // pnlNet al abrir siempre es 0
                trade.notes(),
                null
        );

        Trade savedTrade = tradeRepositoryPort.save(tradeToSave);
        log.info("Trade registrado exitosamente con ID: {}", savedTrade.id());
        
        return savedTrade;
    }

    public List<Trade> getTradesByAccountId(UUID accountId) {
        log.debug("Buscando trades para la cuenta: {}", accountId);
        return tradeRepositoryPort.findByAccountId(accountId);
    }

    public Optional<Trade> getTradeById(UUID tradeId) {
        return tradeRepositoryPort.findById(tradeId);
    }

    public Trade closeTrade(UUID tradeId, BigDecimal exitPrice, LocalDateTime exitDate) {
        log.info("Cerrando trade con ID: {} a precio: {}", tradeId, exitPrice);

        Trade existingTrade = tradeRepositoryPort.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("El trade con ID " + tradeId + " no existe."));

        if ("CLOSED".equals(existingTrade.status())) {
            throw new IllegalStateException("El trade ya se encuentra cerrado.");
        }

        BigDecimal pnlGross = calculatePnL(existingTrade.direction(), existingTrade.entryPrice(), exitPrice, existingTrade.positionSize());

        Trade closedTrade = new Trade(
                existingTrade.id(), existingTrade.accountId(), existingTrade.strategyId(), existingTrade.playbookId(),
                existingTrade.asset(), existingTrade.direction(), "CLOSED",
                existingTrade.entryDate(), exitDate, existingTrade.entryPrice(), exitPrice,
                existingTrade.positionSize(), existingTrade.takeProfit(), existingTrade.stopLoss(),
                existingTrade.plannedRr(), existingTrade.actualRr(), existingTrade.mfePrice(), existingTrade.maePrice(), 
                existingTrade.commissions(), existingTrade.feesAndSwaps(), 
                pnlGross, 
                pnlGross.subtract(defaultToZero(existingTrade.commissions())).subtract(defaultToZero(existingTrade.feesAndSwaps())), 
                existingTrade.notes(),
                existingTrade.imageName()
        );

        return tradeRepositoryPort.save(closedTrade);
    }

    public void updateTradeImage(UUID tradeId, String imageName) {
        Trade existingTrade = tradeRepositoryPort.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("El trade no existe."));

        Trade updatedTrade = new Trade(
                existingTrade.id(), existingTrade.accountId(), existingTrade.strategyId(), existingTrade.playbookId(),
                existingTrade.asset(), existingTrade.direction(), existingTrade.status(),
                existingTrade.entryDate(), existingTrade.exitDate(), existingTrade.entryPrice(), existingTrade.exitPrice(),
                existingTrade.positionSize(), existingTrade.takeProfit(), existingTrade.stopLoss(),
                existingTrade.plannedRr(), existingTrade.actualRr(), existingTrade.mfePrice(), existingTrade.maePrice(), 
                existingTrade.commissions(), existingTrade.feesAndSwaps(), 
                existingTrade.pnlGross(), existingTrade.pnlNet(), existingTrade.notes(),
                imageName // <--- Le inyectamos el nombre de la foto guardada
        );
        tradeRepositoryPort.save(updatedTrade);
    }

    // ==========================================
    // MÉTODOS PRIVADOS DE REGLAS DE NEGOCIO
    // ==========================================

    private void validateNewTrade(Trade trade) {
        if (trade.entryPrice() == null || trade.entryPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Error de validación: El precio de entrada debe ser mayor a 0.");
            throw new IllegalArgumentException("El precio de entrada debe ser mayor a 0.");
        }
        if (trade.positionSize() == null || trade.positionSize().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Error de validación: El tamaño de la posición debe ser mayor a 0.");
            throw new IllegalArgumentException("El tamaño de la posición debe ser mayor a 0.");
        }
    }

    private BigDecimal calculatePnL(String direction, BigDecimal entryPrice, BigDecimal exitPrice, BigDecimal positionSize) {
        BigDecimal priceDifference = "LONG".equalsIgnoreCase(direction) 
                ? exitPrice.subtract(entryPrice) 
                : entryPrice.subtract(exitPrice);
                
        return priceDifference.multiply(positionSize);
    }

    // HELPER: Garantiza que un valor nulo se convierta en 0 matemáticamente seguro.
    private BigDecimal defaultToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}