package com.example.cash.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashOperationResponseDto {
    private boolean success;
    private String message;
    private BigDecimal newBalance;
    private Long accountId;
} 