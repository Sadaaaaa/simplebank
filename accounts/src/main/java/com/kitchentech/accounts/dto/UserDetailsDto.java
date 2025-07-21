package com.kitchentech.accounts.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDetailsDto {
    private Long id;
    private String username;
    private String password;
    private List<String> roles;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
}
