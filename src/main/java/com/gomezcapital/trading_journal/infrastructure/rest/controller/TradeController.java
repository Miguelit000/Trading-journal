package com.gomezcapital.trading_journal.infrastructure.rest.controller;

import com.gomezcapital.trading_journal.application.service.TradeService;
import com.gomezcapital.trading_journal.domain.model.Trade;
import com.gomezcapital.trading_journal.infrastructure.rest.dto.*;
import com.gomezcapital.trading_journal.domain.ports.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final StoragePort storagePort;

    @PostMapping
    public ResponseEntity<TradeResponse> createTrade(@RequestBody CreateTradeRequest request) {
        Trade tradeToCreate = new Trade(
                null, request.portfolioId(), request.strategyId(), null,
                request.asset(), request.direction(), null, null, null,
                request.entryPrice(), null, request.positionSize(), request.takeProfit(), request.stopLoss(),
                null, null, null, null, null, null, null, null, request.notes()
        );
        Trade savedTrade = tradeService.logNewTrade(tradeToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDto(savedTrade));
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<TradeResponse>> getTradesByPortfolio(@PathVariable UUID portfolioId) {
        List<TradeResponse> responseList = tradeService.getTradesByPortfolioId(portfolioId)
                .stream().map(this::toResponseDto).toList();
        return ResponseEntity.ok(responseList);
    }

    // ==========================================
    // NUEVO ENDPOINT: CALENDARIO MENSUAL
    // ==========================================
    @GetMapping("/portfolio/{portfolioId}/calendar")
    public ResponseEntity<List<DailySummaryResponse>> getMonthlyCalendar(
            @PathVariable UUID portfolioId,
            @RequestParam int year,
            @RequestParam int month) {
        
        List<DailySummaryResponse> calendar = tradeService.getMonthlyCalendar(portfolioId, year, month);
        return ResponseEntity.ok(calendar);
    }

    @PatchMapping("/{tradeId}/close")
    public ResponseEntity<TradeResponse> closeTrade(@PathVariable UUID tradeId, @RequestBody CloseTradeRequest request) {
        LocalDateTime exitDate = request.exitDate() != null ? request.exitDate() : LocalDateTime.now();
        Trade closedTrade = tradeService.closeTrade(tradeId, request.exitPrice(), exitDate, request.pnlNet());
        return ResponseEntity.ok(toResponseDto(closedTrade));
    }

    @PutMapping("/{tradeId}")
    public ResponseEntity<TradeResponse> updateTrade(@PathVariable UUID tradeId, @RequestBody UpdateTradeRequest request) {
        Trade updatedTrade = tradeService.updateTrade(tradeId, request);
        return ResponseEntity.ok(toResponseDto(updatedTrade));
    }

    @PostMapping("/{tradeId}/image")
    public ResponseEntity<String> uploadImage(@PathVariable UUID tradeId, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("Archivo vacío.");
        
        String fileName = storagePort.uploadTradeImage(tradeId.toString(), file);
        tradeService.addImageToTrade(tradeId, fileName);
        
        return ResponseEntity.ok("Imagen añadida a la galería exitosamente.");
    }

    @PostMapping("/editor/image")
    public ResponseEntity<?> uploadEditorImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", 0, "message", "Archivo vacío."));
        }
        
        String tempId = "editor_" + UUID.randomUUID().toString().substring(0, 8);
        String fileName = storagePort.uploadTradeImage(tempId, file);
        
        String fileUrl = "http://localhost:8080/api/v1/trades/images/" + fileName; 
        
        return ResponseEntity.ok(Map.of(
            "success", 1,
            "file", Map.of("url", fileUrl)
        ));
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = storagePort.loadTradeImage(filename);
        String contentType = filename.toLowerCase().endsWith(".png") ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @GetMapping("/portfolio/{portfolioId}/export/csv")
    public ResponseEntity<byte[]> exportTradesToCsv(@PathVariable UUID portfolioId) {
        List<Trade> trades = tradeService.getTradesByPortfolioId(portfolioId);
        StringBuilder csv = new StringBuilder("ID,Fecha,Activo,Direccion,Precio Entrada,Tamano,Estado,PnL Neto,Notas\n");
        for (Trade trade : trades) {
            csv.append(trade.id()).append(",").append(trade.entryDate()).append(",")
               .append(trade.asset()).append(",").append(trade.direction()).append(",")
               .append(trade.entryPrice()).append(",").append(trade.positionSize()).append(",")
               .append(trade.status()).append(",").append(trade.pnlNet() != null ? trade.pnlNet() : "0.00").append(",")
               .append(trade.notes() != null ? trade.notes().replace(",", " ") : "").append("\n");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historial_operaciones.csv");
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        return new ResponseEntity<>(csv.toString().getBytes(), headers, HttpStatus.OK);
    }

    private TradeResponse toResponseDto(Trade trade) {
        List<String> images = tradeService.getTradeImages(trade.id());
        return new TradeResponse(
                trade.id(), trade.asset(), trade.direction(), trade.status(),
                trade.entryDate(), trade.entryPrice(), trade.positionSize(), 
                trade.pnlNet(), 
                trade.notes(), 
                images
        );
    }
}