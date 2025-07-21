package com.kitchentech.frontui;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class ExchangeController {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @GetMapping("/exchange-rates")
    public ResponseEntity<?> getExchangeRates(HttpServletRequest request) {
        String url = gatewayUrl + "/api/exchange/rates";
        HttpEntity<?> entity = new HttpEntity<>(createProxyHeaders(request));

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getResponseBodyAsString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении курсов валют: " + e.getMessage()));
        }
    }

    private HttpHeaders createProxyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Список заголовков для копирования
        String[] headersToProxy = {"Cookie", "Authorization", "X-Forwarded-For", "X-Real-IP"};

        for (String headerName : headersToProxy) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                headers.add(headerName, headerValue);
            }
        }

        return headers;
    }
} 