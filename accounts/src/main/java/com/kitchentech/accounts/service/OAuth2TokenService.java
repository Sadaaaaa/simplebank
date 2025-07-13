package com.kitchentech.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2TokenService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public String getAccessToken() {
        log.info("🔐 [OAuth2TokenService] Начинаем получение access token...");
        
        try {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("auth-server")
                    .principal("accounts-service")
                    .build();

            log.info("🔐 [OAuth2TokenService] Создан authorize request для client: auth-server");

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
                log.error("❌ [OAuth2TokenService] Не удалось получить токен доступа");
                return null;
            }

            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            log.info("✅ [OAuth2TokenService] Токен получен успешно: {}", 
                    accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
            
            log.info("🔐 [OAuth2TokenService] Токен действителен до: {}", 
                    authorizedClient.getAccessToken().getExpiresAt());

            return accessToken;

        } catch (Exception e) {
            log.error("❌ [OAuth2TokenService] Ошибка при получении токена: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("⚠️ [OAuth2TokenService] Токен пустой или null");
            return false;
        }
        
        log.info("🔍 [OAuth2TokenService] Проверка токена: {}", 
                token.substring(0, Math.min(20, token.length())) + "...");
        
        // Здесь можно добавить дополнительную валидацию токена
        return true;
    }
} 