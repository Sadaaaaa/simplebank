package com.kitchentech.exchange.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_exchange_facts")
@Data
public class CurrencyExchangeFact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private BigDecimal rate;
    private LocalDateTime date;
    private boolean internal;
} 