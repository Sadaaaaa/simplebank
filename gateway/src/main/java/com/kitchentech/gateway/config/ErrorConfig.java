package com.kitchentech.gateway.config;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class ErrorConfig {
    @Bean
    public ErrorWebExceptionHandler errorWebExceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {
            if (ex instanceof ResponseStatusException) {
                exchange.getResponse().setStatusCode(((ResponseStatusException) ex).getStatusCode());
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap("{\"error\":\"Internal Server Error\"}".getBytes()))
            );
        };
    }
}
