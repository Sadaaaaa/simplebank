package com.kitchentech.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class ErrorConfig {
    // Отключаем кастомный error handler, чтобы Spring Cloud Gateway обрабатывал ошибки по умолчанию
    /*
    @Bean
    public ErrorWebExceptionHandler errorWebExceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {
            log.error("Gateway error: {}", ex.getMessage(), ex);
            
            // Только обрабатываем исключения, которые не являются HTTP ошибками
            if (ex instanceof ResponseStatusException) {
                // Позволяем Spring Cloud Gateway обрабатывать ResponseStatusException
                return Mono.error(ex);
            }
            
            // Для всех остальных исключений возвращаем 500
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            String errorMessage = "{\"error\":\"Internal Server Error\"}";
            
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(errorMessage.getBytes()))
            );
        };
    }
    */
}
