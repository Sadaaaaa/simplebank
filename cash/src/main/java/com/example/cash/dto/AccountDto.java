package com.example.cash.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDto {
    private Long id;
    private Long userId;
    private String username;
    private String currency;
    private String name;
    private BigDecimal balance;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;
} 