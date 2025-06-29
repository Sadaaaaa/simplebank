package com.kitchentech.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("🔧 Настройка SecurityWebFilterChain для gateway");
        
        http
                .authorizeExchange(exchanges -> {
                    log.info("🔒 Настройка авторизации для gateway");
                    exchanges
                            .pathMatchers("/", "/index", "/login", "/register", "/register-success", "/dashboard", "/login**").permitAll()
                            .pathMatchers("/actuator/**").permitAll()
                            .pathMatchers("/api/**").permitAll()
                            .anyExchange().authenticated();
                    log.info("✅ Авторизация настроена: /api/** разрешен без аутентификации");
                })
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable());

        log.info("✅ SecurityWebFilterChain настроен");
        return http.build();
    }
}
