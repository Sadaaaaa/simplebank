package com.example.cash.controller;

import com.example.cash.dto.CashOperationDto;
import com.example.cash.dto.CashOperationResponseDto;
import com.example.cash.dto.AccountDto;
import com.example.cash.entity.Account;
import com.example.cash.repository.AccountRepository;
import com.example.cash.service.CashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {
    
    private final CashService cashService;
    private final AccountRepository accountRepository;
    
    @PostMapping("/operation")
    public ResponseEntity<CashOperationResponseDto> performOperation(@RequestBody CashOperationDto operationDto) {
        log.info("Получен запрос на операцию: {}", operationDto);
        
        CashOperationResponseDto response = cashService.performOperation(operationDto);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/accounts/{username}")
    public ResponseEntity<List<AccountDto>> getUserAccounts(@PathVariable String username) {
        log.info("Получение активных счетов для пользователя: {}", username);
        
        List<Account> accounts = accountRepository.findByUsernameAndDeletedAtIsNull(username);
        List<AccountDto> accountDtos = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        log.info("Найдено {} активных счетов для пользователя {}", accountDtos.size(), username);
        return ResponseEntity.ok(accountDtos);
    }
    
    @GetMapping("/accounts/user/{userId}")
    public ResponseEntity<List<AccountDto>> getUserAccountsById(@PathVariable Long userId) {
        log.info("Получение активных счетов для пользователя с ID: {}", userId);
        
        List<Account> accounts = accountRepository.findByUserIdAndDeletedAtIsNull(userId);
        List<AccountDto> accountDtos = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        log.info("Найдено {} активных счетов для пользователя с ID {}", accountDtos.size(), userId);
        return ResponseEntity.ok(accountDtos);
    }
    
    @GetMapping("/accounts/id/{accountId}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long accountId) {
        log.info("Получение счета по ID: {}", accountId);
        
        try {
            Account account = accountRepository.findByIdAndDeletedAtIsNull(accountId)
                    .orElse(null);
            
            if (account != null) {
                AccountDto dto = convertToDto(account);
                log.info("Найден счет с ID: {}", accountId);
                return ResponseEntity.ok(dto);
            } else {
                log.warn("Счет с ID {} не найден", accountId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при получении счета с ID {}: {}", accountId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountDto accountDto) {
        log.info("Создание нового счета: {}", accountDto);
        
        try {
            List<Account> existingAccounts = accountRepository.findByUserIdAndDeletedAtIsNull(accountDto.getUserId());
            boolean hasCurrencyAccount = existingAccounts.stream()
                    .anyMatch(acc -> acc.getCurrency().equals(accountDto.getCurrency()));
            
            if (hasCurrencyAccount) {
                log.warn("У пользователя {} уже есть счет в валюте {}", accountDto.getUserId(), accountDto.getCurrency());
                return ResponseEntity.badRequest().build();
            }
            
            Account account = new Account();
            account.setUserId(accountDto.getUserId());
            account.setUsername(accountDto.getUsername());
            account.setCurrency(accountDto.getCurrency());
            account.setName(accountDto.getName() != null ? accountDto.getName() : accountDto.getCurrency() + " счет");
            account.setBalance(accountDto.getBalance() != null ? accountDto.getBalance() : BigDecimal.ZERO);
            account.setActive(true);
            
            Account savedAccount = accountRepository.save(account);
            AccountDto savedDto = convertToDto(savedAccount);
            
            log.info("Создан новый счет с ID: {}", savedAccount.getId());
            return ResponseEntity.ok(savedDto);
            
        } catch (Exception e) {
            log.error("Ошибка при создании счета: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/accounts/{accountId}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long accountId, @RequestBody AccountDto accountDto) {
        log.info("Обновление счета с ID: {}", accountId);
        
        try {
            Account account = accountRepository.findByIdAndDeletedAtIsNull(accountId)
                    .orElseThrow(() -> new RuntimeException("Счет не найден"));
            
            if (accountDto.getName() != null) {
                account.setName(accountDto.getName());
            }
            
            Account savedAccount = accountRepository.save(account);
            AccountDto savedDto = convertToDto(savedAccount);
            
            log.info("Счет с ID {} обновлен", accountId);
            return ResponseEntity.ok(savedDto);
            
        } catch (Exception e) {
            log.error("Ошибка при обновлении счета: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId, @RequestParam(required = false) String deletedBy) {
        log.info("Soft delete счета с ID: {} пользователем: {}", accountId, deletedBy);
        
        try {
            Account account = accountRepository.findByIdAndDeletedAtIsNull(accountId)
                    .orElseThrow(() -> new RuntimeException("Счет не найден"));
            
            if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                log.warn("Невозможно удалить счет с ненулевым балансом: {}", account.getBalance());
                return ResponseEntity.badRequest().build();
            }
            
            account.setActive(false);
            account.setDeletedAt(LocalDateTime.now());
            account.setDeletedBy(deletedBy != null ? deletedBy : "system");
            
            accountRepository.save(account);
            
            log.info("Счет с ID {} помечен как удаленный (soft delete)", accountId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Ошибка при soft delete счета: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private AccountDto convertToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setUserId(account.getUserId());
        dto.setUsername(account.getUsername());
        dto.setCurrency(account.getCurrency());
        dto.setName(account.getName());
        dto.setBalance(account.getBalance());
        dto.setActive(account.getActive());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        dto.setDeletedAt(account.getDeletedAt());
        dto.setDeletedBy(account.getDeletedBy());
        return dto;
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Cash service is running");
    }
} 