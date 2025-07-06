package com.kitchentech.frontui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @PostMapping("/internal")
    public ResponseEntity<Map> performInternalTransfer(@RequestBody Map<String, Object> request) {
        log.info("🔄 Проксирование запроса на внутренний перевод: {}", request);
        
        try {
            String url = gatewayUrl + "/api/transfer/internal";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.info("✅ Ответ от transfer сервиса: {}", response.getBody());
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("❌ Ошибка при проксировании запроса на внутренний перевод: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Ошибка при выполнении перевода: " + e.getMessage()));
        }
    }

    @PostMapping("/external")
    public ResponseEntity<Map> performExternalTransfer(@RequestBody Map<String, Object> request) {
        log.info("🔄 Проксирование запроса на внешний перевод: {}", request);
        
        try {
            String url = gatewayUrl + "/api/transfer/external";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.info("✅ Ответ от transfer сервиса: {}", response.getBody());
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("❌ Ошибка при проксировании запроса на внешний перевод: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Ошибка при выполнении перевода: " + e.getMessage()));
        }
    }

    @GetMapping("/accounts/{username}")
    public ResponseEntity<List<Map<String, Object>>> getUserAccounts(@PathVariable String username) {
        log.info("🔄 Проксирование запроса на получение счетов пользователя: {}", username);
        
        try {
            String url = gatewayUrl + "/api/transfer/accounts/" + username;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> accounts = java.util.Arrays.stream(response.getBody())
                        .map(item -> (Map<String, Object>) item)
                        .toList();
                
                log.info("✅ Получено {} счетов для пользователя {}", accounts.size(), username);
                return ResponseEntity.ok(accounts);
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при получении счетов пользователя: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(List.of());
    }
} 