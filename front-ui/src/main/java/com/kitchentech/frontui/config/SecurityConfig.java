package com.kitchentech.frontui.config;

import com.kitchentech.frontui.dto.UserDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/css/**", "/dashboard").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                );
        return http.build();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

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

    @Bean
    public UserDetailsService userDetailsService(RestTemplate restTemplate) {
        return username -> {
            log.info("🔍 Поиск пользователя: {}", username);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            String url = gatewayUrl + "/api/users/" + username;
            log.info("📡 Запрос к: {}", url);

            try {
                ResponseEntity<UserDetailsDto> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        UserDetailsDto.class
                );

                log.info("✅ Получен ответ: статус={}, тело={}", response.getStatusCode(), response.getBody());

                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                    log.warn("❌ Неверный статус или пустое тело ответа");
                    throw new UsernameNotFoundException("User not found");
                }

                UserDetailsDto user = response.getBody();
                log.info("👤 Пользователь найден: {}", user.getUsername());
                
                return User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRoles().toArray(new String[0]))
                        .build();
            } catch (HttpClientErrorException e) {
                log.error("❌ HTTP ошибка при поиске пользователя: статус={}, тело={}", e.getStatusCode(), e.getResponseBodyAsString());
                throw new UsernameNotFoundException("User not found", e);
            } catch (Exception e) {
                log.error("❌ Ошибка при поиске пользователя: {}", e.getMessage(), e);
                throw new UsernameNotFoundException("User not found", e);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
