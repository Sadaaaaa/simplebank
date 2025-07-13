package com.kitchentech.accounts.controller;

import com.kitchentech.accounts.service.OAuth2TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/token-test")
@RequiredArgsConstructor
public class TokenTestController {

    private final OAuth2TokenService oAuth2TokenService;

    @GetMapping("/get-token")
    public ResponseEntity<Map<String, Object>> getToken() {
        log.info("🔐 [TokenTestController] Запрос на получение токена");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = oAuth2TokenService.getAccessToken();
            
            if (token != null) {
                log.info("✅ [TokenTestController] Токен получен успешно");
                response.put("success", true);
                response.put("token", token.substring(0, Math.min(20, token.length())) + "...");
                response.put("message", "Токен получен успешно");
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ [TokenTestController] Не удалось получить токен");
                response.put("success", false);
                response.put("message", "Не удалось получить токен");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("❌ [TokenTestController] Ошибка при получении токена: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken() {
        log.info("🔍 [TokenTestController] Запрос на валидацию токена");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = oAuth2TokenService.getAccessToken();
            boolean isValid = oAuth2TokenService.validateToken(token);
            
            response.put("success", isValid);
            response.put("token_valid", isValid);
            response.put("message", isValid ? "Токен валиден" : "Токен невалиден");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [TokenTestController] Ошибка при валидации токена: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 