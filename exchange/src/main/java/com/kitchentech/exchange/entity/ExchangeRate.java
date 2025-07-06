package com.kitchentech.exchange.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
@Data
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromCurrency;

    @Column(nullable = false)
    private String toCurrency;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
} 