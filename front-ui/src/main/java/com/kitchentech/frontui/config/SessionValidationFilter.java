package com.kitchentech.frontui.config;

import com.kitchentech.frontui.dto.UserDetailsDto;
import com.kitchentech.frontui.helpers.SessionSetter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class SessionValidationFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public SessionValidationFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("🔍 SessionValidationFilter получил запрос: {} {}", request.getMethod(), request.getRequestURI());

        if (isPublicResource(request.getRequestURI())) {
            log.info("✅ Публичный ресурс, пропускаем: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🔐 Проверяем сессию для: {}", request.getRequestURI());
        
        if (validateSession(request)) {
            UserDetailsDto userDetailsDto = getUserDetails(request);
            if (userDetailsDto != null && userDetailsDto.getUsername() != null) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetailsDto.getUsername(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("✅ Сессия валидна, пропускаем запрос");
                filterChain.doFilter(request, response);
            } else {
                log.warn("❌ Не удалось получить данные пользователя, перенаправляем на логин");
                response.sendRedirect("/login");
            }
        } else {
            log.warn("❌ Сессия невалидна, перенаправляем на логин");
            response.sendRedirect("/login");
        }
    }

    private boolean isPublicResource(String uri) {
        return uri.equals("/login") ||
                uri.equals("/register") ||
                uri.equals("/register-success") ||
                uri.equals("/error") ||
                uri.equals("/logout") ||
                uri.equals("/") ||
                uri.startsWith("/css/");
    }

    private boolean validateSession(HttpServletRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        headers.add("Cookie", "JSESSIONID=" + cookie.getValue());
                    }
                }
            }

            HttpEntity<?> entity = new HttpEntity<>(headers);

            String url = gatewayUrl + "/api/public/session/validate";
            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Void.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Ошибка при проверке сессии: {}", e.getMessage());
            return false;
        }
    }

    private UserDetailsDto getUserDetails(HttpServletRequest request) {
        HttpEntity<?> entity = new HttpEntity<>(SessionSetter.createProxyHeaders(request));

        try {
            String url = gatewayUrl + "/api/users/me";
            ResponseEntity<UserDetailsDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserDetailsDto.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Ошибка при получении информации о пользователе, {} ", e.getMessage());
        }
        return null;
    }
}