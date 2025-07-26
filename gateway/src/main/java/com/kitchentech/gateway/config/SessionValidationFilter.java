package com.kitchentech.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SessionValidationFilter implements WebFilter {
    @Value("${accounts.url:http://localhost:8082}")
    private String accountsUrl;

    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        // Пропускаем сервисные запросы с JWT
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        
        // Пропускаем открытые эндпоинты
        if (path.startsWith("/api/public/") || path.startsWith("/actuator/") || path.startsWith("/api/login")) {
            return chain.filter(exchange);
        }
        
        // Проверяем JSESSIONID для остальных
        HttpCookie jsession = request.getCookies().getFirst("JSESSIONID");
        if (jsession == null) {
            log.warn("Нет JSESSIONID, доступ запрещён: {}", request.getPath());
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Проверяем сессию через accounts
        String validateUrl = accountsUrl + "/public/session/validate";
        return webClient.get()
                .uri(validateUrl)
                .cookie("JSESSIONID", jsession.getValue())
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        // Добавляем заголовок о том, что сессия валидна
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("X-Gateway-Session-Valid", "true")
                                .header("X-Gateway-Session-Id", jsession.getValue())
                                .build();
                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(mutatedRequest)
                                .build();
                        return chain.filter(mutatedExchange);
                    } else {
                        log.warn("Сессия невалидна для {}: {}", request.getPath(), response.statusCode());
                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                });
    }
} 