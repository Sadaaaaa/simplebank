package com.kitchentech.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class SessionValidationFilter implements GlobalFilter, Ordered {
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/users/login",
            "/api/users/register",
            "/api/login"
    );

    private static final List<String> OPEN_PATTERNS = List.of(
            "/api/users/" // это сделает открытыми все /api/users/*
    );

    private final WebClient webClient = WebClient.create("http://localhost:8082"); // accounts service

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Проверяем точные совпадения или паттерны
        boolean isOpenEndpoint = !path.startsWith("/api/") ||
                OPEN_ENDPOINTS.stream().anyMatch(path::startsWith) ||
                OPEN_PATTERNS.stream().anyMatch(path::startsWith);

        if (isOpenEndpoint) {
            return chain.filter(exchange);
        }

        HttpCookie jsession = exchange.getRequest().getCookies().getFirst("JSESSIONID");
        if (jsession == null) {
            log.warn("Нет JSESSIONID, доступ запрещён: {}", path);
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        // Проверяем сессию через accounts
        return webClient.get()
                .uri("/users/session/validate")
                .cookie("JSESSIONID", jsession.getValue())
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return chain.filter(exchange);
                    } else {
                        log.warn("Сессия невалидна для {}: {}", path, response.statusCode());
                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
} 