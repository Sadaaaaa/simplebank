package com.kitchentech.exchange.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CurrencyExchangeFactDto {
    private Long userId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private BigDecimal rate;
    private LocalDateTime date;
    private boolean internal;
} 