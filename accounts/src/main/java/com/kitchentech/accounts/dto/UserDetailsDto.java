package com.kitchentech.accounts.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDetailsDto {
    private String username;
    private String password;
    private List<String> roles;
}
