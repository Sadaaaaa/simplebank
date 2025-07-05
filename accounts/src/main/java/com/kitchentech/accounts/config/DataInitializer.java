package com.kitchentech.accounts.config;

import com.kitchentech.accounts.entity.User;
import com.kitchentech.accounts.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@Component
@Configuration
@EnableJpaAuditing
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, есть ли уже пользователь "user"
        if (userRepository.findByUsername("user").isEmpty()) {
            log.info("Создание тестового пользователя 'user'");
            
            User user = new User();
            user.setUsername("user");
            user.setPassword(new BCryptPasswordEncoder().encode("password"));
            user.setEmail("user@example.com");
            user.setFirstName("Тестовый");
            user.setLastName("Пользователь");
            user.setRoles("USER");
            user.setEnabled(true);
            
            userRepository.save(user);
            log.info("Тестовый пользователь 'user' создан");
        } else {
            log.info("Тестовый пользователь 'user' уже существует");
        }
    }
} 