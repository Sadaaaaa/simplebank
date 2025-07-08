package com.kitchentech.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("✅ Custom RouteLocator bean created");
        log.info("🔍 Создаем маршруты...");
        return builder.routes()
                .route("front_ui_route", r -> r
                        .path("/", "/login", "/register", "/register-success", "/dashboard", "/index")
                        .filters(f -> f
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Front-UI route: {} -> {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://front-ui"))
                .route("front_ui_api_route", r -> r
                        .path("/api/cash/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Front-UI API route: {} -> {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://cash"))
                .route("accounts_route", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .stripPrefix(1)
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Accounts route: {} -> {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://accounts"))
                .route("users_route", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .stripPrefix(1)
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Users route: {} -> {} (Registration request)", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    log.info("📤 Request headers: {}", exchange.getRequest().getHeaders());
                                    log.info("📥 Response status: {}", exchange.getResponse().getStatusCode());
                                    log.info("📄 Response body: {}", s);
                                    
                                    if (s != null && s.contains("<!DOCTYPE html>")) {
                                        log.warn("⚠️ HTML response detected, converting to JSON error");
                                        return Mono.just("{\"error\":\"Not Found\"}");
                                    }
                                    
                                    // Возвращаем пустую строку если тело ответа null
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://accounts"))
                .route("auth_route", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Auth route: {} -> {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://auth-server"))
                .route("transfer_route", r -> r
                        .path("/api/transfer/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Transfer route: {} -> {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://transfer"))
                .route("exchange_route", r -> r
                        .path("/api/exchange/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Transfer route: {} -> {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://exchange"))
                .route("blocker_route", r -> r
                        .path("/api/blocker/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Transfer route: {} -> {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://blocker"))
                .route("notifications_route", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .preserveHostHeader()
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    log.info("🔄 Transfer route: {} -> {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                                    return Mono.just(s != null ? s : "");
                                })
                        )
                        .uri("lb://notifications"))
                .build();
    }
}
