package com.kitchentech.frontui.controller;

import com.kitchentech.frontui.dto.UserRegistrationDto;
import com.kitchentech.frontui.dto.UserRegistrationResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequestMapping("/")
@Controller
@RequiredArgsConstructor
public class IndexController {

    private final RestTemplate restTemplate;
    @Value("${gateway.url}")
    private String gatewayUrl;

    @GetMapping
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("register")
    public String register() {
        return "register";
    }

    @GetMapping("register-success")
    public String registerSuccess() {
        return "register-success";
    }

    @PostMapping("register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam String email,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String birthDate,
                               Model model) {

        log.info("🔄 Начало регистрации пользователя: {}", username);

        // Простая валидация
        if (!password.equals(confirmPassword)) {
            log.warn("❌ Пароли не совпадают для пользователя: {}", username);
            model.addAttribute("error", "Пароли не совпадают");
            return "register";
        }

        if (password.length() < 6) {
            log.warn("❌ Слишком короткий пароль для пользователя: {}", username);
            model.addAttribute("error", "Пароль должен содержать минимум 6 символов");
            return "register";
        }

        try {
            // Создаем DTO для регистрации
            UserRegistrationDto registrationDto = new UserRegistrationDto();
            registrationDto.setUsername(username);
            registrationDto.setPassword(password);
            registrationDto.setEmail(email);
            registrationDto.setFirstName(firstName);
            registrationDto.setLastName(lastName);
            registrationDto.setBirthDate(java.time.LocalDate.parse(birthDate));

            log.info("📤 Отправка данных регистрации: username={}, email={}, birthDate={}", username, email, birthDate);

            // Настраиваем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UserRegistrationDto> entity = new HttpEntity<>(registrationDto, headers);

            // Вызываем API регистрации
            String url = gatewayUrl + "/api/public/register";
            log.info("🌐 URL для регистрации: {}", url);

            ResponseEntity<UserRegistrationResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    UserRegistrationResponseDto.class
            );

            log.info("📥 Получен ответ: статус={}, тело={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Успешная регистрация
                log.info("✅ Пользователь успешно зарегистрирован: {}", username);
                return "redirect:/register-success";
            } else {
                log.warn("⚠️ Неуспешный ответ при регистрации: {}", response.getStatusCode());
                model.addAttribute("error", "Ошибка при регистрации");
                return "register";
            }

        } catch (Exception e) {
            log.error("❌ Ошибка при регистрации пользователя {}: {}", username, e.getMessage(), e);

            // Обрабатываем ошибки
            String errorMessage = "Ошибка при регистрации";
            if (e.getMessage().contains("Username already exists")) {
                errorMessage = "Пользователь с таким именем уже существует";
            } else if (e.getMessage().contains("Email already exists")) {
                errorMessage = "Пользователь с таким email уже существует";
            }

            model.addAttribute("error", errorMessage);
            return "register";
        }
    }

    @PostMapping("/logout-me")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("🔄 AuthController.logout() вызван!");
        log.info("📤 Request URI: {}", request.getRequestURI());
        log.info("📤 Request method: {}", request.getMethod());
        log.info("📤 Request URL: {}", request.getRequestURL());

        try {
            // Копируем куки сессии
            HttpHeaders headers = new HttpHeaders();
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        headers.add("Cookie", "JSESSIONID=" + cookie.getValue());
                        log.info("🍪 Найден JSESSIONID: {}", cookie.getValue());
                    }
                }
            }

            HttpEntity<?> entity = new HttpEntity<>(headers);

            // Отправляем logout запрос в accounts
            String url = gatewayUrl + "/api/users/logout";
            log.info("🌐 Отправляем logout запрос на: {}", url);
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            // Очищаем куки в браузере
            Cookie sessionCookie = new Cookie("JSESSIONID", "");
            sessionCookie.setMaxAge(0);
            sessionCookie.setPath("/");
            response.addCookie(sessionCookie);
            log.info("✅ Logout выполнен успешно");

            return "redirect:/login?logout";
        } catch (Exception e) {
            log.error("❌ Ошибка при logout: {}", e.getMessage(), e);
            return "redirect:/login?error";
        }
    }
}
