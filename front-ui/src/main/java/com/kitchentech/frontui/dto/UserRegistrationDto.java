package com.kitchentech.frontui.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
} 