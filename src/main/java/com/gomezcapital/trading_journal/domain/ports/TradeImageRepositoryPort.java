package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.TradeImage;
import java.util.List;
import java.util.UUID;

public interface TradeImageRepositoryPort {
    
    TradeImage save(TradeImage tradeImage);
    List<TradeImage> findByTradeId(UUID tradeId);
    void deleteById(UUID id);
    
}
