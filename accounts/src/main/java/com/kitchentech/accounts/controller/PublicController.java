package com.kitchentech.accounts.controller;

import com.kitchentech.accounts.dto.UserRegistrationDto;
import com.kitchentech.accounts.dto.UserRegistrationResponseDto;
import com.kitchentech.accounts.entity.User;
import com.kitchentech.accounts.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContext;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

//    @PostMapping("/login")
//    public ResponseEntity<?> login(
//            @RequestParam String username,
//            @RequestParam String password,
//            HttpServletRequest request,
//            HttpServletResponse response
//    ) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(username, password)
//            );
//            // Устанавливаем аутентификацию в SecurityContext
//            request.getSession(true); // создаём сессию, если нет
//            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
//            return ResponseEntity.ok().body(Map.of("message", "Login successful"));
//        } catch (AuthenticationException e) {
//            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
//        }
//    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDto> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        log.info("🔄 Получен запрос на регистрацию пользователя: {}", registrationDto.getUsername());
        log.info("📧 Email: {}, Имя: {}, Фамилия: {}, Дата рождения: {}", registrationDto.getEmail(), registrationDto.getFirstName(), registrationDto.getLastName(), registrationDto.getBirthDate());
        try {
            if (userRepository.findByUsernameAndDeletedAtIsNull(registrationDto.getUsername()).isPresent()) {
                log.warn("❌ Пользователь с username {} уже существует", registrationDto.getUsername());
                UserRegistrationResponseDto errorResponse = new UserRegistrationResponseDto();
                errorResponse.setUsername(registrationDto.getUsername());
                errorResponse.setEmail(registrationDto.getEmail());
                errorResponse.setFirstName(registrationDto.getFirstName());
                errorResponse.setLastName(registrationDto.getLastName());
                errorResponse.setMessage("Username already exists");
                errorResponse.setSuccess(false);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (userRepository.findByEmailAndDeletedAtIsNull(registrationDto.getEmail()).isPresent()) {
                log.warn("❌ Пользователь с email {} уже существует", registrationDto.getEmail());
                UserRegistrationResponseDto errorResponse = new UserRegistrationResponseDto();
                errorResponse.setUsername(registrationDto.getUsername());
                errorResponse.setEmail(registrationDto.getEmail());
                errorResponse.setFirstName(registrationDto.getFirstName());
                errorResponse.setLastName(registrationDto.getLastName());
                errorResponse.setMessage("Email already exists");
                errorResponse.setSuccess(false);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            User user = new User();
            user.setUsername(registrationDto.getUsername());
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setEmail(registrationDto.getEmail());
            user.setFirstName(registrationDto.getFirstName());
            user.setLastName(registrationDto.getLastName());
            user.setBirthDate(registrationDto.getBirthDate());
            user.setRoles("USER");
            user.setEnabled(true);
            User savedUser = userRepository.save(user);
            log.info("✅ Пользователь успешно зарегистрирован: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
            UserRegistrationResponseDto successResponse = new UserRegistrationResponseDto();
            successResponse.setUsername(savedUser.getUsername());
            successResponse.setEmail(savedUser.getEmail());
            successResponse.setFirstName(savedUser.getFirstName());
            successResponse.setLastName(savedUser.getLastName());
            successResponse.setMessage("User registered successfully");
            successResponse.setSuccess(true);
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            log.error("❌ Ошибка при регистрации пользователя {}: {}", registrationDto.getUsername(), e.getMessage(), e);
            UserRegistrationResponseDto errorResponse = new UserRegistrationResponseDto();
            errorResponse.setUsername(registrationDto.getUsername());
            errorResponse.setEmail(registrationDto.getEmail());
            errorResponse.setFirstName(registrationDto.getFirstName());
            errorResponse.setLastName(registrationDto.getLastName());
            errorResponse.setMessage("Internal server error: " + e.getMessage());
            errorResponse.setSuccess(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    @PostMapping("/{username}/restore")
    public ResponseEntity<Map<String, String>> restoreUser(@PathVariable String username) {
        log.info("🔄 [restore-user] Восстановление пользователя: {}", username);
        return userRepository.findByUsernameAndDeletedAtIsNotNull(username)
                .map(user -> {
                    log.info("🔍 [restore-user] Удаленный пользователь найден: {}", username);
                    user.setEnabled(true);
                    user.setDeletedAt(null);
                    user.setDeletedBy(null);
                    userRepository.save(user);
                    log.info("✅ [restore-user] Пользователь успешно восстановлен: {}", username);
                    return ResponseEntity.ok(Map.of("message", "Пользователь успешно восстановлен"));
                })
                .orElseGet(() -> {
                    log.warn("❌ [restore-user] Удаленный пользователь не найден: {}", username);
                    return ResponseEntity.status(404).body(Map.of("message", "Удаленный пользователь не найден"));
                });
    }

    @GetMapping("/session/validate")
    public ResponseEntity<?> validateSession(HttpSession session) {
        log.info("SESSION VALIDATE CALLED, session: {}", session.getId());

        // Используйте правильную константу
        Object securityContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        if (securityContext != null) {
            SecurityContext context = (SecurityContext) securityContext;
            Authentication authentication = context.getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    !(authentication instanceof AnonymousAuthenticationToken)) {
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.status(401).build();
    }
} 