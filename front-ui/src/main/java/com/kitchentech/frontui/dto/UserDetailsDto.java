package com.kitchentech.frontui.dto;

import lombok.Data;

@Data
public class UserDetailsDto {
    private String username;
    private String password;
    private String roles;
    private Boolean enabled;
}
