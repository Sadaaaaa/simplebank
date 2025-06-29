package com.kitchentech.frontui;

import com.kitchentech.frontui.dto.ChangePasswordRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
} 