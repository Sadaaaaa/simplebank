package com.example.cash.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashOperationDto {
    private Long accountId;
    private BigDecimal amount;
    private String operationType;
} 