package com.kitchentech.frontui.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDetailsDto {
    private String username;
    private String password;
    private String roles;
    private Boolean enabled;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
}
