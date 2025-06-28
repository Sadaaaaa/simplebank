package com.kitchentech.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("✅ Custom RouteLocator bean created");
        return builder.routes()
                .route("front_ui_route", r -> r
                        .path("/", "/login", "/dashboard", "/index")
                        .filters(f -> f
                                .preserveHostHeader()
                        )
                        .uri("lb://front-ui"))
                .route("accounts_route", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f
                                .addRequestHeader("Accept", "application/json")
                                .addRequestHeader("Content-Type", "application/json")
                                .addResponseHeader("Content-Type", "application/json")
                                .stripPrefix(1)
                                .preserveHostHeader()
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
                                    if (s != null && s.contains("<!DOCTYPE html>")) {
                                        return Mono.just("{\"error\":\"Not Found\"}");
                                    }
                                    return Mono.just(s);
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
                        )
                        .uri("lb://auth-server"))
                .build();
    }
}
