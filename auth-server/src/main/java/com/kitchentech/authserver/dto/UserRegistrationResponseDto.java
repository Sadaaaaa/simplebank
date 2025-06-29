package com.kitchentech.authserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationResponseDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String message;
    private boolean success;
} 