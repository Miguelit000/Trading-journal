package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.TradeService;
import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.CloseTradeRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.CreateTradeRequest;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.TradeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController // Indica que esta clase expone endpoints REST y devuelve JSON
@RequestMapping("/api/v1/trades") // La URL base para todos los métodos de esta clase
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    /**
     * Endpoint para CREAR un trade nuevo.
     * Método HTTP: POST
     * URL: http://localhost:8080/api/v1/trades
     */
    @PostMapping
    public ResponseEntity<TradeResponse> createTrade(@RequestBody CreateTradeRequest request) {
        log.info("API REST: Petición recibida para crear un trade en la cuenta: {}", request.accountId());

        // 1. Traducir de DTO a modelo de Dominio
        Trade tradeToCreate = new Trade(
                null, 
                request.accountId(), 
                request.strategyId(), 
                request.playbookId(),
                request.asset(), 
                request.direction(), 
                null, // status se asigna en el servicio
                null, // entryDate se asigna en el servicio
                null, // exitDate
                request.entryPrice(), 
                null, // exitPrice
                request.positionSize(),
                request.takeProfit(), 
                request.stopLoss(), 
                BigDecimal.ZERO, // plannedRr
                BigDecimal.ZERO, // actualRr
                null, // mfePrice (puede ser null hasta que se cierre)
                null, // maePrice (puede ser null hasta que se cierre)
                BigDecimal.ZERO, // commissions
                BigDecimal.ZERO, // feesAndSwaps
                BigDecimal.ZERO, // pnlGross
                BigDecimal.ZERO, // pnlNet
                request.notes()
        );

        // 2. Procesar con el Servicio (Las reglas de negocio)
        Trade savedTrade = tradeService.logNewTrade(tradeToCreate);

        // 3. Traducir la respuesta a DTO y devolver código HTTP 201 (Created)
        TradeResponse response = toResponseDto(savedTrade);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para OBTENER todos los trades de una cuenta.
     * Método HTTP: GET
     * URL: http://localhost:8080/api/v1/trades/account/{accountId}
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TradeResponse>> getTradesByAccount(@PathVariable UUID accountId) {
        log.info("API REST: Solicitando trades de la cuenta: {}", accountId);
        
        List<Trade> trades = tradeService.getTradesByAccount(accountId);
        
        List<TradeResponse> responseList = trades.stream()
                .map(this::toResponseDto)
                .toList();

        return ResponseEntity.ok(responseList); // Devuelve código HTTP 200 (OK)
    }

    // ==========================================
    // MÉTODO PRIVADO (Mapper)
    // ==========================================

    private TradeResponse toResponseDto(Trade trade) {
        return new TradeResponse(
                trade.id(),
                trade.asset(),
                trade.direction(),
                trade.status(),
                trade.entryDate(),
                trade.entryPrice(),
                trade.positionSize(),
                trade.pnlNet()
        );
    }

     /**
     * Endpoint para CERRAR un trade y calcular sus ganancias.
     * Método HTTP: PATCH
     * URL: http://localhost:8080/api/v1/trades/{tradeId}/close
     */
    @PatchMapping("/{tradeId}/close")
    public ResponseEntity<TradeResponse> closeTrade(
            @PathVariable UUID tradeId,
            @RequestBody CloseTradeRequest request) {
        
        log.info("API REST: Petición recibida para cerrar el trade: {}", tradeId);

        // Si el frontend no nos manda una fecha exacta de cierre, tomamos la hora actual
        LocalDateTime exitDate = request.exitDate() != null ? request.exitDate() : LocalDateTime.now();

        // Llamamos a nuestro Servicio para que aplique las reglas de negocio y las matemáticas
        Trade closedTrade = tradeService.closeTrade(tradeId, request.exitPrice(), exitDate);

        // Convertimos el resultado a DTO y lo devolvemos
        TradeResponse response = toResponseDto(closedTrade);
        return ResponseEntity.ok(response);
    }
}