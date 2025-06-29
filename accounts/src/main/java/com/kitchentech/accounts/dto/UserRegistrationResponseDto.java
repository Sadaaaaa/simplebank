package com.kitchentech.accounts.dto;

import lombok.Data;

@Data
public class UserRegistrationResponseDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String message;
    private Boolean success;
} 