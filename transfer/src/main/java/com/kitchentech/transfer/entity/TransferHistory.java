package com.kitchentech.transfer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_history")
@Data
public class TransferHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fromUserId;
    private Long toUserId;
    private Long fromAccountId;
    private Long toAccountId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private BigDecimal rate;
    private LocalDateTime date;
    private boolean allowed;
    private String blockReason;
    private boolean internal;
} 