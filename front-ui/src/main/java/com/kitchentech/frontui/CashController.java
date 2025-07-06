package com.kitchentech.frontui;

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
    public ResponseEntity<Map> performOperation(@RequestBody Map<String, Object> operationData) {
        log.info("🔄 CashController: выполнение операции {}", operationData);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/cash/operation";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(operationData, headers);

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
    public ResponseEntity<List<Map>> getUserAccounts(@PathVariable String username) {
        log.info("🔄 CashController: получение счетов для пользователя {}", username);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/cash/accounts/" + username;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object[].class
            );
            log.info("✅ Ответ от gateway для получения счетов: {}", response.getStatusCode());
            
            // Преобразуем Object[] в List<Map>
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
    public ResponseEntity<List<Map>> getUserAccountsById(@PathVariable Long userId) {
        log.info("🔄 CashController: получение счетов для пользователя с ID {}", userId);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/cash/accounts/user/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object[].class
            );
            log.info("✅ Ответ от gateway для получения счетов по userId: {}", response.getStatusCode());
            
            // Преобразуем Object[] в List<Map>
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
    public ResponseEntity<Map> createAccount(@RequestBody Map<String, Object> accountData) {
        log.info("🔄 CashController: создание нового счета {}", accountData);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/cash/accounts";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(accountData, headers);

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
    public ResponseEntity<Map> updateAccount(@PathVariable Long accountId, @RequestBody Map<String, Object> accountData) {
        log.info("🔄 CashController: обновление счета с ID {}: {}", accountId, accountData);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/cash/accounts/" + accountId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(accountData, headers);

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
    public ResponseEntity<Map> deleteAccount(@PathVariable Long accountId) {
        log.info("🔄 CashController: удаление счета с ID {}", accountId);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/cash/accounts/" + accountId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);

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

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> health() {
        log.info("🔄 CashController: проверка здоровья");
        return ResponseEntity.ok("Cash controller is running");
    }
} 