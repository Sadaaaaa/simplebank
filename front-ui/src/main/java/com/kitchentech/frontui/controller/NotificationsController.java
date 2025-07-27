package com.kitchentech.frontui.controller;

import com.kitchentech.frontui.helpers.SessionSetter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class NotificationsController {
    private final RestTemplate restTemplate;
    @Value("${gateway.url}")
    private String gatewayUrl;

    @GetMapping("/notifications/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestParam Long userId, HttpServletRequest request) {
        String url = gatewayUrl + "/api/notifications/unread?userId=" + userId;
        log.info("🔔 Запрос непрочитанных уведомлений для userId={}, URL={}", userId, url);

        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));
        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Object.class
            );
            log.info("✅ Получен ответ от notifications: статус={}, тип контента={}", 
                    response.getStatusCode(), response.getHeaders().getContentType());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ Ошибка при получении непрочитанных уведомлений: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении непрочитанных уведомлений: " + e.getMessage()));
        }
    }

    @GetMapping("/notifications/all")
    public ResponseEntity<?> getAllNotifications(@RequestParam Long userId, HttpServletRequest request) {
        String url = gatewayUrl + "/api/notifications/all?userId=" + userId;
        log.info("🔔 Запрос всех уведомлений для userId={}, URL={}", userId, url);

        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));
        
        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Object.class
            );
            log.info("✅ Получен ответ от notifications: статус={}, тип контента={}", 
                    response.getStatusCode(), response.getHeaders().getContentType());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ Ошибка при получении всех уведомлений: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении всех уведомлений: " + e.getMessage()));
        }
    }

    @PostMapping("/notifications/read/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        String url = gatewayUrl + "/api/notifications/read/" + id;
        log.info("🔔 Отметка уведомления как прочитанного: id={}, URL={}", id, url);

        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));
        
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Void.class
            );
            log.info("✅ Уведомление отмечено как прочитанное: статус={}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (Exception e) {
            log.error("❌ Ошибка при отметке уведомления как прочитанного: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при отметке уведомления как прочитанного: " + e.getMessage()));
        }
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestParam Long userId, HttpServletRequest request) {
        String url = gatewayUrl + "/api/notifications/unread-count?userId=" + userId;
        log.info("🔔 Запрос количества непрочитанных уведомлений для userId={}, URL={}", userId, url);

        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));
        
        try {
            ResponseEntity<Long> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Long.class
            );
            log.info("✅ Получено количество непрочитанных уведомлений: статус={}, количество={}", 
                    response.getStatusCode(), response.getBody());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ Ошибка при получении количества непрочитанных уведомлений: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении количества непрочитанных уведомлений: " + e.getMessage()));
        }
    }
} 