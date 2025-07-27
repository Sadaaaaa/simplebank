package com.kitchentech.accounts.config;

import com.kitchentech.accounts.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/public/session/validate").permitAll()
                        .requestMatchers("/users/register", "/users/login", "/login", "/actuator/**", "/token-test/**", "/users/session/validate", "/users/*", "/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .successHandler((request, response, authentication) -> {
                            log.info("SUCCESS HANDLER CALLED!");
                            try {
                                response.setStatus(HttpStatus.OK.value());
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.getWriter().write("{\"success\":true,\"error\":null}");
                                response.getWriter().flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            log.info("FAILURE HANDLER CALLED! {}", exception.getMessage());
                            try {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.getWriter().write("{\"success\":false,\"error\":\"" + exception.getMessage() + "\"}");
                                response.getWriter().flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles("USER") // или user.getRoles().split(",")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
} 