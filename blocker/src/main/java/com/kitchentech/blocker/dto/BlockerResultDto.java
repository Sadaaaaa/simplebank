package com.kitchentech.blocker.dto;

import lombok.Data;

@Data
public class BlockerResultDto {
    private boolean allowed;
    private String reason;
}

