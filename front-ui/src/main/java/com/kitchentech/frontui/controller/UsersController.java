package com.kitchentech.frontui.controller;

import com.kitchentech.frontui.helpers.SessionSetter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class UsersController {
    private final RestTemplate restTemplate;
    @Value("${gateway.url}")
    private String gatewayUrl;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        String url = gatewayUrl + "/api/users";
        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));
        try {
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении пользователей: " + e.getMessage()));
        }
    }
} 