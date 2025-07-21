package com.kitchentech.frontui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@RestController
public class AuthController {
    private final RestTemplate restTemplate;
    @Value("${gateway.url}")
    private String gatewayUrl;

    public AuthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response) {
        // Копируем параметры формы
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "username=" + username + "&password=" + password;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // Проксируем на accounts через gateway
        ResponseEntity<String> resp = restTemplate.exchange(
                gatewayUrl + "/api/login",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Копируем Set-Cookie из ответа accounts в ответ клиенту
        List<String> cookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            for (String cookie : cookies) {
                response.addHeader(HttpHeaders.SET_COOKIE, cookie);
            }
        }

        // Редиректим на dashboard или возвращаем статус
        if (resp.getStatusCode().is3xxRedirection()) {
            String location = resp.getHeaders().getFirst(HttpHeaders.LOCATION);
            return ResponseEntity.status(resp.getStatusCode()).header(HttpHeaders.LOCATION, location).build();
        }
        return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
    }
}
