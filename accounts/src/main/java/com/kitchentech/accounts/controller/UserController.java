package com.kitchentech.accounts.controller;

import com.kitchentech.accounts.dto.UserDetailsDto;
import com.kitchentech.accounts.entity.User;
import com.kitchentech.accounts.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUser(@PathVariable String username) {
        log.info("Поиск пользователя: {}", username);

        return userRepository.findByUsername(username)
                .map(user -> {
                    UserDetailsDto userDto = new UserDetailsDto();
                    userDto.setUsername(user.getUsername());
                    userDto.setPassword(user.getPassword());
                    userDto.setRoles(user.getRoles());
                    userDto.setEnabled(user.getEnabled());
                    
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body((Object) userDto);
                })
                .orElseGet(() -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "User not found");
                    errorResponse.put("message", "User with username '" + username + "' does not exist");
                    return ResponseEntity.status(404)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body((Object) errorResponse);
                });
    }
}
