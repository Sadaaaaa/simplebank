package com.kitchentech.frontui.controller;

import com.kitchentech.frontui.dto.ChangePasswordRequestDto;
import com.kitchentech.frontui.dto.UserDetailsDto;
import com.kitchentech.frontui.dto.UserRegistrationDto;
import com.kitchentech.frontui.helpers.SessionSetter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public DashboardController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String dashboard(HttpServletRequest request, Model model) {
        log.info("🔄 DashboardController: отображение dashboard - ЗАПРОС ПОЛУЧЕН!");
        log.info("🔍 Request URL: {}", request.getRequestURL());
        log.info("🔍 Request method: {}", request.getMethod());
        
        String username = "Гость";
        try {
            // Прокидываем JSESSIONID из куки в запрос к /me
            HttpHeaders headers = new HttpHeaders();
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        headers.add("Cookie", "JSESSIONID=" + cookie.getValue());
                    }
                }
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);

            // Запрос к accounts через gateway
            String url = gatewayUrl + "/api/users/me";
            ResponseEntity<UserDetailsDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserDetailsDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UserDetailsDto userDetails = response.getBody();
                username = userDetails.getUsername();
                model.addAttribute("userDetails", userDetails);
                log.info("✅ Данные пользователя загружены через /me: {}", username);
            }
        } catch (Exception e) {
            log.warn("⚠️ Не удалось загрузить данные пользователя через /me: {}", e.getMessage());
        }

        model.addAttribute("username", username);
        return "dashboard";
    }

    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequestDto request, HttpServletRequest servletRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        log.info("🔄 DashboardController: смена пароля для пользователя {}", username);
        request.setUsername(username);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/users/change-password";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChangePasswordRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            log.info("✅ Ответ от gateway: {}", response.getStatusCode());
            String message = response.getBody() != null ? (String) response.getBody().get("message") : "";
            return ResponseEntity.status(response.getStatusCode()).body(message);
        } catch (Exception e) {
            log.error("❌ Ошибка при смене пароля через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при смене пароля: " + e.getMessage());
        }
    }

    @PostMapping("/update-profile")
    @ResponseBody
    public ResponseEntity<String> updateProfile(@RequestBody UserRegistrationDto profileDto, HttpServletRequest servletRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        log.info("🔄 DashboardController: обновление профиля для пользователя {}", username);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/users/" + username + "/profile";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserRegistrationDto> entity = new HttpEntity<>(profileDto, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );
            log.info("✅ Ответ от gateway: {}", response.getStatusCode());
            String message = response.getBody() != null ? (String) response.getBody().get("message") : "";
            return ResponseEntity.status(response.getStatusCode()).body(message);
        } catch (Exception e) {
            log.error("❌ Ошибка при обновлении профиля через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении профиля: " + e.getMessage());
        }
    }

    @PostMapping("/delete-account")
    @ResponseBody
    public ResponseEntity<String> deleteAccount(HttpServletRequest servletRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        log.info("🔄 DashboardController: удаление аккаунта для пользователя {}", username);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/users/" + username;
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
            log.info("✅ Ответ от gateway: {}", response.getStatusCode());
            String message = response.getBody() != null ? (String) response.getBody().get("message") : "";
            return ResponseEntity.status(response.getStatusCode()).body(message);
        } catch (Exception e) {
            log.error("❌ Ошибка при удалении аккаунта через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при удалении аккаунта: " + e.getMessage());
        }
    }

    @GetMapping("/user-info")
    @ResponseBody
    public ResponseEntity<Map> getUserInfo(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        log.info("🔄 DashboardController: получение информации о пользователе {}", username);

        // Формируем запрос к gateway
        String url = gatewayUrl + "/api/users/me";
        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            log.info("✅ Ответ от gateway для получения информации о пользователе: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ Ошибка при получении информации о пользователе через gateway: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ошибка при получении информации о пользователе: " + e.getMessage()));
        }
    }
} 