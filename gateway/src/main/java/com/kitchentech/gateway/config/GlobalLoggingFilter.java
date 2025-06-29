package com.kitchentech.gateway.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
class GlobalLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("🌐 Входящий запрос: {} {} -> {}",
                request.getMethod(),
                request.getPath(),
                request.getHeaders().get("Host"));
        log.info("📋 Заголовки запроса: {}", request.getHeaders());

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    log.info("📤 Исходящий ответ: {} {} -> {}",
                            request.getMethod(),
                            request.getPath(),
                            exchange.getResponse().getStatusCode());
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
