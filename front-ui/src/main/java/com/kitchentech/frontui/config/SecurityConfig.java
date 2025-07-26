package com.kitchentech.frontui.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/register-success", "/error", "/css/**", "/dashboard", "/logout", "/").permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public RestTemplate restTemplate(SessionCookieInterceptor sessionCookieInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(sessionCookieInterceptor));

        // Добавляем все необходимые конвертеры
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        // Используем стандартный Jackson конвертер только для JSON
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();

        messageConverters.add(jacksonConverter);
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new ResourceRegionHttpMessageConverter());
        messageConverters.add(new FormHttpMessageConverter());

        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }
}
