package com.kitchentech.frontui.controller;

import com.kitchentech.frontui.dto.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
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
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "username=" + username + "&password=" + password;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            System.out.println("🔄 Отправляем логин запрос на: " + gatewayUrl + "/api/login");
            ResponseEntity<LoginResponseDto> resp = restTemplate.exchange(
                    gatewayUrl + "/api/login",
                    HttpMethod.POST,
                    entity,
                    LoginResponseDto.class
            );
            System.out.println("✅ Получен ответ: " + resp.getStatusCode() + " body: " + resp.getBody());
            System.out.println("🔍 Проверяем успешность: status=" + resp.getStatusCode().is2xxSuccessful() + 
                             ", body=" + (resp.getBody() != null) + 
                             ", success=" + (resp.getBody() != null ? resp.getBody().isSuccess() : "null"));

            List<String> cookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null) {
                for (String cookie : cookies) {
                    response.addHeader(HttpHeaders.SET_COOKIE, cookie);
                }
            }

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null && resp.getBody().isSuccess()) {
                System.out.println("🔄 Выполняем редирект на /dashboard");
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, "/dashboard")
                        .build();
            } else {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, "/login?error")
                        .build();
            }

        } catch (Exception e) {
            System.out.println("❌ Login exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login?error")
                    .build();
        }
    }


}
