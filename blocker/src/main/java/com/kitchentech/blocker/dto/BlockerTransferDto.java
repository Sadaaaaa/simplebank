package com.kitchentech.blocker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockerTransferDto {
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String currency;
}

