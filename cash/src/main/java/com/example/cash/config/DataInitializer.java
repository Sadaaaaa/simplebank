package com.example.cash.config;

import com.example.cash.entity.Account;
import com.example.cash.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Инициализация тестовых данных для cash сервиса...");

        // Очищаем старые данные для пересоздания с правильным username
        accountRepository.deleteAll();
        log.info("🗑️ Очищены старые данные");

        // Проверяем, есть ли уже данные
        if (accountRepository.count() == 0) {
            log.info("📝 Создание тестовых счетов...");

            // Создаем тестовые счета
            Account account1 = new Account();
            account1.setUserId(1L); // ID пользователя Asdfg1
            account1.setUsername("Asdfg1");
            account1.setCurrency("RUB");
            account1.setName("Основной счет");
            account1.setBalance(new BigDecimal("50000.00"));
            account1.setActive(true);
            accountRepository.save(account1);

            Account account2 = new Account();
            account2.setUserId(1L); // ID пользователя Asdfg1
            account2.setUsername("Asdfg1");
            account2.setCurrency("USD");
            account2.setName("Долларовый счет");
            account2.setBalance(new BigDecimal("1500.00"));
            account2.setActive(true);
            accountRepository.save(account2);

            Account account3 = new Account();
            account3.setUserId(1L); // ID пользователя Asdfg1
            account3.setCurrency("EUR");
            account3.setUsername("Asdfg1");
            account3.setName("Евро счет");
            account3.setBalance(new BigDecimal("1200.00"));
            account3.setActive(true);
            accountRepository.save(account3);

            log.info("✅ Создано {} тестовых счетов", accountRepository.count());
        } else {
            log.info("ℹ️ Тестовые данные уже существуют ({} счетов)", accountRepository.count());
        }
        // Добавляем счета для второго пользователя (userId=2, username='user2')
        if (accountRepository.findAll().stream().noneMatch(a -> a.getUserId() != null && a.getUserId() == 2L)) {
            log.info("Создание счетов для пользователя user2 (id=2)");
            Account acc1 = new Account();
            acc1.setUserId(2L);
            acc1.setUsername("user2");
            acc1.setCurrency("RUB");
            acc1.setName("Основной счет");
            acc1.setBalance(new BigDecimal("100000.00"));
            acc1.setActive(true);
            accountRepository.save(acc1);

            Account acc2 = new Account();
            acc2.setUserId(2L);
            acc2.setUsername("user2");
            acc2.setCurrency("USD");
            acc2.setName("Долларовый счет");
            acc2.setBalance(new BigDecimal("2000.00"));
            acc2.setActive(true);
            accountRepository.save(acc2);

            Account acc3 = new Account();
            acc3.setUserId(2L);
            acc3.setUsername("user2");
            acc3.setCurrency("EUR");
            acc3.setName("Евро счет");
            acc3.setBalance(new BigDecimal("1500.00"));
            acc3.setActive(true);
            accountRepository.save(acc3);
            log.info("Счета для пользователя user2 созданы");
        }
    }
} 