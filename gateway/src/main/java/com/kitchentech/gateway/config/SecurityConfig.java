package com.kitchentech.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
public class SecurityConfig {

    @Autowired
    private SessionValidationFilter sessionValidationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .addFilterAt(sessionValidationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> {
                    exchanges
                            .pathMatchers("/", "/index", "/login", "/register", "/register-success", "/dashboard", "/logout").permitAll()
                            .pathMatchers("/actuator/**").permitAll()
                            .pathMatchers("/api/public/**").permitAll()
                            .pathMatchers("/api/login").permitAll()
                            .anyExchange().permitAll();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable);
        return http.build();
    }


}
