package com.kitchentech.transfer.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountInfoDto {
    private Long id;
    private Long userId;
    private String username;
    private String currency;
    private String name;
    private BigDecimal balance;
    private Boolean active;
} 