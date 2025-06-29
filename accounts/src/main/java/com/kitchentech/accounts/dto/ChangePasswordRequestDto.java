package com.kitchentech.accounts.dto;

import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    private String username;
    private String newPassword;
} 