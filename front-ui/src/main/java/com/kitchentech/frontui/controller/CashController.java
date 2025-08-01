package com.kitchentech.frontui.controller;

import com.kitchentech.frontui.helpers.SessionSetter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/api/cash")
public class CashController {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public CashController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/operation")
    @ResponseBody
    public ResponseEntity<?> performOperation(@RequestBody Map<String, Object> operationData, HttpServletRequest request) {
        log.info("🔄 CashController: выполнение операции {}", operationData);

        String url = gatewayUrl + "/api/cash/operation";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(operationData, SessionSetter.createProxyHeaders(request));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            log.info("✅ Ответ от gateway для cash операции: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ Ошибка при выполнении cash операции через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Ошибка при выполнении операции: " + e.getMessage()));
        }
    }

    @GetMapping("/accounts/{username}")
    @ResponseBody
    public ResponseEntity<?> getUserAccounts(@PathVariable String username, HttpServletRequest request) {
        log.info("🔄 CashController: получение счетов для пользователя {}", username);

        String url = gatewayUrl + "/api/cash/accounts/" + username;
        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));
        try {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object[].class
            );
            log.info("✅ Ответ от gateway для получения счетов: {}", response.getStatusCode());
            
            List<Map> accounts = java.util.Arrays.stream(response.getBody())
                    .map(item -> (Map) item)
                    .collect(Collectors.toList());
            
            return ResponseEntity.status(response.getStatusCode()).body(accounts);
        } catch (Exception e) {
            log.error("❌ Ошибка при получении счетов через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/accounts/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserAccountsById(@PathVariable Long userId, HttpServletRequest request) {
        log.info("🔄 CashController: получение счетов для пользователя с ID {}", userId);

        String url = gatewayUrl + "/api/cash/accounts/user/" + userId;
        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));

        try {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object[].class
            );
            log.info("✅ Ответ от gateway для получения счетов по userId: {}", response.getStatusCode());
            
            List<Map> accounts = java.util.Arrays.stream(response.getBody())
                    .map(item -> (Map) item)
                    .collect(Collectors.toList());
            
            return ResponseEntity.status(response.getStatusCode()).body(accounts);
        } catch (Exception e) {
            log.error("❌ Ошибка при получении счетов по userId через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PostMapping("/accounts")
    @ResponseBody
    public ResponseEntity<?> createAccount(@RequestBody Map<String, Object> accountData, HttpServletRequest request) {
        log.info("🔄 CashController: создание нового счета {}", accountData);

        String url = gatewayUrl + "/api/cash/accounts";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(accountData, SessionSetter.createProxyHeaders(request));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            log.info("✅ Ответ от gateway для создания счета: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ Ошибка при создании счета через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Ошибка при создании счета: " + e.getMessage()));
        }
    }

    @PutMapping("/accounts/{accountId}")
    @ResponseBody
    public ResponseEntity<?> updateAccount(@PathVariable Long accountId, @RequestBody Map<String, Object> accountData, HttpServletRequest request) {
        log.info("🔄 CashController: обновление счета с ID {}: {}", accountId, accountData);

        String url = gatewayUrl + "/api/cash/accounts/" + accountId;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(accountData, SessionSetter.createProxyHeaders(request));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );
            log.info("✅ Ответ от gateway для обновления счета: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ Ошибка при обновлении счета через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Ошибка при обновлении счета: " + e.getMessage()));
        }
    }

    @DeleteMapping("/accounts/{accountId}")
    @ResponseBody
    public ResponseEntity<?> deleteAccount(@PathVariable Long accountId, HttpServletRequest request) {
        log.info("🔄 CashController: удаление счета с ID {}", accountId);

        String url = gatewayUrl + "/api/cash/accounts/" + accountId;
        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    Map.class
            );
            log.info("✅ Ответ от gateway для удаления счета: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ Ошибка при удалении счета через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Ошибка при удалении счета: " + e.getMessage()));
        }
    }
} 