package com.kitchentech.transfer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferResponseDto {
    private boolean success;
    private String message;
    private String transferId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private BigDecimal fromAccountNewBalance;
    private BigDecimal toAccountNewBalance;
    private LocalDateTime transferDate;
} 