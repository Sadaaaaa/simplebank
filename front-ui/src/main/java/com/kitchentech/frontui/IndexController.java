package com.kitchentech.frontui;

import com.kitchentech.frontui.dto.UserRegistrationDto;
import com.kitchentech.frontui.dto.UserRegistrationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.kitchentech.frontui.dto.UserDetailsDto;

@Slf4j
@RequestMapping("/")
@Controller
public class IndexController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @GetMapping
    public String index() {
        // Временно всегда перенаправляем на dashboard для тестирования
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
    
    @GetMapping("register-success")
    public String registerSuccess() {
        return "register-success";
    }
    
    @GetMapping("dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        String username = "Гость";
        try {
            // Прокидываем JSESSIONID из куки в запрос к /me
            HttpHeaders headers = new HttpHeaders();
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        headers.add("Cookie", "JSESSIONID=" + cookie.getValue());
                    }
                }
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);

            // Запрос к accounts через gateway
            String url = gatewayUrl + "/api/users/me";
            ResponseEntity<UserDetailsDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserDetailsDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UserDetailsDto userDetails = response.getBody();
                username = userDetails.getUsername();
                model.addAttribute("userDetails", userDetails);
                log.info("✅ Данные пользователя загружены через /me: {}", username);
            }
        } catch (Exception e) {
            log.warn("⚠️ Не удалось загрузить данные пользователя через /me: {}", e.getMessage());
        }

        model.addAttribute("username", username);
        return "dashboard";
    }
    
    @GetMapping("logout")
    public String logoutPage() {
        return "logout";
    }
    
    @PostMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            // Очищаем сессию
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        return "redirect:/logout";
    }
}
