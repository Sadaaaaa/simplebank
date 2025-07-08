package com.kitchentech.accounts.controller;

import com.kitchentech.accounts.dto.UserRegistrationDto;
import com.kitchentech.accounts.dto.UserRegistrationResponseDto;
import com.kitchentech.accounts.dto.ChangePasswordRequestDto;
import com.kitchentech.accounts.dto.UserDetailsDto;
import com.kitchentech.accounts.entity.User;
import com.kitchentech.accounts.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            // Проверяем, существует ли активный пользователь с таким username
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

            // Проверяем, существует ли активный пользователь с таким email
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
        log.info("🔍 Поиск активного пользователя по username: {}", username);

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(user -> {
                    log.info("✅ Пользователь найден: {}", username);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("❌ Пользователь не найден: {}", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public List<UserDetailsDto> getAllUsers() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(user -> {
                    UserDetailsDto dto = new UserDetailsDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
//                    dto.setEmail(user.getEmail());
//                    dto.setFirstName(user.getFirstName());
//                    dto.setLastName(user.getLastName());
                    dto.setRoles(user.getRoles() != null ? List.of(user.getRoles().split(",")) : List.of());
                    return dto;
                })
                .collect(Collectors.toList());
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
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
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

    @PutMapping("/{username}/profile")
    public ResponseEntity<Map<String, String>> updateProfile(@PathVariable String username, 
                                                           @RequestBody UserRegistrationDto profileDto) {
        log.info("🔄 [update-profile] Обновление профиля для пользователя: {}", username);
        log.info("📧 [update-profile] Новые данные: firstName={}, lastName={}, email={}, birthDate={}", 
                profileDto.getFirstName(), profileDto.getLastName(), profileDto.getEmail(), profileDto.getBirthDate());

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(user -> {
                    log.info("🔍 [update-profile] Пользователь найден: {}", username);
                    
                    // Проверяем, не занят ли email другим активным пользователем
                    if (!user.getEmail().equals(profileDto.getEmail())) {
                        if (userRepository.findByEmailAndDeletedAtIsNull(profileDto.getEmail()).isPresent()) {
                            log.warn("❌ [update-profile] Email {} уже занят другим пользователем", profileDto.getEmail());
                            return ResponseEntity.badRequest().body(Map.of("message", "Email уже занят другим пользователем"));
                        }
                    }
                    
                    // Обновляем данные
                    user.setFirstName(profileDto.getFirstName());
                    user.setLastName(profileDto.getLastName());
                    user.setEmail(profileDto.getEmail());
                    user.setBirthDate(profileDto.getBirthDate());
                    
                    userRepository.save(user);
                    log.info("✅ [update-profile] Профиль успешно обновлён для пользователя: {}", username);
                    return ResponseEntity.ok(Map.of("message", "Профиль успешно обновлён"));
                })
                .orElseGet(() -> {
                    log.warn("❌ [update-profile] Пользователь не найден: {}", username);
                    return ResponseEntity.status(404).body(Map.of("message", "Пользователь не найден"));
                });
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username, 
                                                         @RequestParam(required = false) String deletedBy) {
        log.info("🔄 [delete-user] Soft delete пользователя: {} пользователем: {}", username, deletedBy);

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(user -> {
                    log.info("🔍 [delete-user] Пользователь найден: {}", username);
                    
                    // Здесь можно добавить дополнительную логику проверки
                    // Например, проверку баланса счетов, активных операций и т.д.
                    
                    // Soft delete - помечаем как удаленный
                    user.setEnabled(false);
                    user.setDeletedAt(LocalDateTime.now());
                    user.setDeletedBy(deletedBy != null ? deletedBy : "system");
                    
                    userRepository.save(user);
                    log.info("✅ [delete-user] Пользователь помечен как удаленный (soft delete): {}", username);
                    return ResponseEntity.ok(Map.of("message", "Пользователь успешно удалён"));
                })
                .orElseGet(() -> {
                    log.warn("❌ [delete-user] Пользователь не найден: {}", username);
                    return ResponseEntity.status(404).body(Map.of("message", "Пользователь не найден"));
                });
    }

    @PostMapping("/{username}/restore")
    public ResponseEntity<Map<String, String>> restoreUser(@PathVariable String username) {
        log.info("🔄 [restore-user] Восстановление пользователя: {}", username);

        return userRepository.findByUsernameAndDeletedAtIsNotNull(username)
                .map(user -> {
                    log.info("🔍 [restore-user] Удаленный пользователь найден: {}", username);
                    
                    // Восстанавливаем пользователя
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
}
