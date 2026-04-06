package com.gomezcapital.trading_journal.infrastructure.rest.dto;
import java.math.BigDecimal;
public record CreatePortfolioRequest(String name, BigDecimal initialBalance, String currency) {}