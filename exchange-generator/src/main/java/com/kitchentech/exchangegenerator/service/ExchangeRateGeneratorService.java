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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateGeneratorService {

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientManager authorizedClientManager; // Добавить

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Value("${auth.server.url}")
    private String authServerUrl;
    @Value("${auth.client.id}")
    private String clientId;
    @Value("${auth.client.secret}")
    private String clientSecret;

    private static final List<String> CURRENCIES = Arrays.asList("RUB", "USD", "EUR");
    private static final String BASE_CURRENCY = "RUB";
    
    // Базовые курсы (примерные)
    private static final Map<String, BigDecimal> BASE_RATES = Map.of(
        "USD", new BigDecimal("0.011"), // 1 RUB = 0.011 USD
        "EUR", new BigDecimal("0.010")  // 1 RUB = 0.010 EUR
    );

    @Scheduled(fixedRate = 60000) // Каждую минуту
    public void generateAndSendExchangeRates() {
        log.info("🔄 Генерация курсов валют...");
        
        try {
            // Генерируем курсы для всех пар валют
            List<ExchangeRateDto> rates = generateExchangeRates();

            // Отправляем в exchange сервис через gateway
            sendRatesToExchangeService(rates);
            
            log.info("✅ Курсы валют сгенерированы и отправлены");
            
        } catch (Exception e) {
            log.error("❌ Ошибка при генерации или отправке курсов валют: {}", e.getMessage(), e);
        }
    }

    private List<ExchangeRateDto> generateExchangeRates() {
        List<ExchangeRateDto> rates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Генерируем курсы для всех пар валют
        for (String fromCurrency : CURRENCIES) {
            for (String toCurrency : CURRENCIES) {
                if (!fromCurrency.equals(toCurrency)) {
                    BigDecimal rate = generateRate(fromCurrency, toCurrency);
                    
                    ExchangeRateDto rateDto = new ExchangeRateDto();
                    rateDto.setFromCurrency(fromCurrency);
                    rateDto.setToCurrency(toCurrency);
                    rateDto.setRate(rate);
                    rateDto.setTimestamp(now);
                    
                    rates.add(rateDto);
                }
            }
        }
        
        return rates;
    }

    private BigDecimal generateRate(String fromCurrency, String toCurrency) {
        // Если одна из валют - базовая (RUB), используем прямой курс
        if (fromCurrency.equals(BASE_CURRENCY)) {
            return getBaseRate(toCurrency);
        } else if (toCurrency.equals(BASE_CURRENCY)) {
            return BigDecimal.ONE.divide(getBaseRate(fromCurrency), 4, RoundingMode.HALF_UP);
        } else {
            // Для конвертации между небазовыми валютами: через RUB
            BigDecimal fromToRub = BigDecimal.ONE.divide(getBaseRate(fromCurrency), 4, RoundingMode.HALF_UP);
            BigDecimal rubToTo = getBaseRate(toCurrency);
            return fromToRub.multiply(rubToTo).setScale(4, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal getBaseRate(String currency) {
        BigDecimal baseRate = BASE_RATES.get(currency);
        if (baseRate == null) {
            return BigDecimal.ONE;
        }
        
        // Добавляем случайное изменение ±5%
        double randomChange = 0.95 + (Math.random() * 0.1); // 0.95 - 1.05
        return baseRate.multiply(BigDecimal.valueOf(randomChange)).setScale(4, RoundingMode.HALF_UP);
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
            log.info("🔐 Получен токен доступа: {}", accessToken.substring(0, Math.min(20, accessToken.length())) + "...");


            String url = gatewayUrl + "/api/exchange/rates";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken); // Добавить токен в заголовок

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