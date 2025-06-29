package com.kitchentech.frontui.dto;

import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    private String username;
    private String newPassword;
} 