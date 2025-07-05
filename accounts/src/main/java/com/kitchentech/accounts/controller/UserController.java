package com.kitchentech.accounts.controller;

import com.kitchentech.accounts.dto.UserRegistrationDto;
import com.kitchentech.accounts.dto.UserRegistrationResponseDto;
import com.kitchentech.accounts.dto.ChangePasswordRequestDto;
import com.kitchentech.accounts.entity.User;
import com.kitchentech.accounts.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDto> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        log.info("🔄 Получен запрос на регистрацию пользователя: {}", registrationDto.getUsername());
        log.info("📧 Email: {}, Имя: {}, Фамилия: {}, Дата рождения: {}", registrationDto.getEmail(), registrationDto.getFirstName(), registrationDto.getLastName(), registrationDto.getBirthDate());

        try {
            // Проверяем, существует ли пользователь с таким username
            if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
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

            // Проверяем, существует ли пользователь с таким email
            if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
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

            // Создаем нового пользователя
            User user = new User();
            user.setUsername(registrationDto.getUsername());
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setEmail(registrationDto.getEmail());
            user.setFirstName(registrationDto.getFirstName());
            user.setLastName(registrationDto.getLastName());
            user.setBirthDate(registrationDto.getBirthDate());
            user.setRoles("USER");
            user.setEnabled(true);

            // Сохраняем пользователя
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

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        log.info("🔍 Поиск пользователя по username: {}", username);

        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("✅ Пользователь найден: {}", username);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("❌ Пользователь не найден: {}", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequestDto request) {
        String username = request.getUsername();
        log.info("🔄 [change-password] Username из тела запроса: {}", username);
        log.info("🔄 [change-password] Новый пароль: {}", request.getNewPassword());
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            log.warn("❌ [change-password] Новый пароль не может быть пустым для пользователя: {}", username);
            return ResponseEntity.badRequest().body(Map.of("message", "Пароль не может быть пустым"));
        }
        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("🔍 [change-password] Пользователь найден: {}", username);
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    userRepository.save(user);
                    log.info("✅ [change-password] Пароль успешно изменён для пользователя: {}", username);
                    return ResponseEntity.ok(Map.of("message", "Пароль успешно изменён"));
                })
                .orElseGet(() -> {
                    log.warn("❌ [change-password] Пользователь не найден: {}", username);
                    return ResponseEntity.status(404).body(Map.of("message", "Пользователь не найден"));
                });
    }
}
