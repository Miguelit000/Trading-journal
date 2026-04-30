package com.gomezcapital.trading_journal.domain.ports;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;


public interface StoragePort {

    String uploadTradeImage(String tradeId, MultipartFile file);

    Resource loadTradeImage(String filename);
    
}
