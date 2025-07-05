package com.example.cash.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDto {
    private Long id;
    private Long userId;
    private String username;
    private String currency;
    private String name;
    private BigDecimal balance;
    private Boolean active;
} 