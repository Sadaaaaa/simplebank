package com.kitchentech.exchangegenerator.service;

import com.kitchentech.exchangegenerator.dto.ExchangeRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {
    private final RestTemplate restTemplate;
    private final GeneratorService generatorService;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Value("${auth.server.url}")
    private String authServerUrl;
    @Value("${auth.client.id}")
    private String clientId;
    @Value("${auth.client.secret}")
    private String clientSecret;

    @Scheduled(fixedRate = 60000) // Каждую минуту
    public void generateAndSendExchangeRates() {
        log.info("🔄 Генерация курсов валют...");

        try {
            // Генерируем курсы для всех пар валют
            List<ExchangeRateDto> rates = generatorService.generateExchangeRates();

            // Отправляем в exchange сервис через gateway
            sendRatesToExchangeService(rates);

            log.info("✅ Курсы валют сгенерированы и отправлены");

        } catch (Exception e) {
            log.error("❌ Ошибка при генерации или отправке курсов валют: {}", e.getMessage(), e);
        }
    }

    private void sendRatesToExchangeService(List<ExchangeRateDto> rates) {
        try {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("auth-server")
                    .principal("exchange-generator-service") // любой уникальный идентификатор
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
                log.error("❌ Не удалось получить токен доступа");
                return;
            }

            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            log.info("[OAUTH2] Получен токен у auth-server: {}... (expires: {})", accessToken.substring(0, Math.min(20, accessToken.length())), authorizedClient.getAccessToken().getExpiresAt());

            String url = gatewayUrl + "/api/exchange/rates";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken); // Добавить токен в заголовок

            log.info("[OAUTH2] Отправка запроса с токеном в gateway: {}...", accessToken.substring(0, Math.min(20, accessToken.length())));

            HttpEntity<List<ExchangeRateDto>> entity = new HttpEntity<>(rates, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("📤 Курсы валют успешно отправлены в exchange сервис");
            } else {
                log.warn("⚠️ Получен неожиданный ответ от exchange сервиса: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при отправке курсов в exchange сервис: {}", e.getMessage(), e);
        }
    }
}
