package com.kitchentech.accounts.controller;

import com.kitchentech.accounts.dto.UserDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{username}")
    public ResponseEntity<UserDetailsDto> getUser(@PathVariable String username) {

        log.warn("username " + username);

        if (!"user".equals(username)) {
            return ResponseEntity.notFound().build();
        }

        UserDetailsDto user = new UserDetailsDto();
        user.setUsername("user");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRoles(List.of("USER"));

        return ResponseEntity.ok(user);
    }
}
