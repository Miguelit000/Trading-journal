package com.gomezcapital.trading_journal.domain.ports;

import org.springframework.web.multipart.MultipartFile;

public interface StoragePort {

    String uploadTradeImage(String tradeId, MultipartFile file);
    
}
