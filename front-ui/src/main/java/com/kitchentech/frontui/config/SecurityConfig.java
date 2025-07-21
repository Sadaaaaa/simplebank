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

//    @Value("${gateway.url}")
//    private String gatewayUrl;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http, RestTemplate restTemplate) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/login", "/register", "/register-success", "/error", "/css/**", "/dashboard", "/logout").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/dashboard", true)
//                        .permitAll()
//                )
//                .rememberMe(remember -> remember
//                        .key("simplebank-remember-me-key")
//                        .tokenValiditySeconds(60 * 60 * 24 * 30) // 30 дней
//                        .rememberMeParameter("remember-me")
//                        .userDetailsService(userDetailsService(restTemplate))
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/dashboard", true)
//                );
//        return http.build();
//    }
//
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
//
//    @Bean
//    public UserDetailsService userDetailsService(RestTemplate restTemplate) {
//        return username -> {
//            log.info("🔍 Поиск пользователя: {}", username);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<?> entity = new HttpEntity<>(headers);
//            String url = gatewayUrl + "/api/public/" + username;
//            log.info("📡 Запрос к: {}", url);
//
//            try {
//                ResponseEntity<UserDetailsDto> response = restTemplate.exchange(
//                        url,
//                        HttpMethod.GET,
//                        entity,
//                        UserDetailsDto.class
//                );
//
//                log.info("✅ Получен ответ: статус={}, тело={}", response.getStatusCode(), response.getBody());
//
//                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
//                    log.warn("❌ Неверный статус или пустое тело ответа");
//                    throw new UsernameNotFoundException("User not found");
//                }
//
//                UserDetailsDto user = response.getBody();
//                log.info("👤 Пользователь найден: {}", user.getUsername());
//
//                return User.withUsername(user.getUsername())
//                        .password(user.getPassword())
//                        .roles(user.getRoles())
//                        .build();
//            } catch (HttpClientErrorException e) {
//                log.error("❌ HTTP ошибка при поиске пользователя: статус={}, тело={}", e.getStatusCode(), e.getResponseBodyAsString());
//                throw new UsernameNotFoundException("User not found", e);
//            } catch (Exception e) {
//                log.error("❌ Ошибка при поиске пользователя: {}", e.getMessage(), e);
//                throw new UsernameNotFoundException("User not found", e);
//            }
//        };
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
}
