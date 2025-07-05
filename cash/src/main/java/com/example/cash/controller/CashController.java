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
        log.info("Получение счетов для пользователя: {}", username);
        
        List<Account> accounts = accountRepository.findByUsernameAndActiveTrue(username);
        List<AccountDto> accountDtos = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        log.info("Найдено {} счетов для пользователя {}", accountDtos.size(), username);
        return ResponseEntity.ok(accountDtos);
    }
    
    @GetMapping("/accounts/user/{userId}")
    public ResponseEntity<List<AccountDto>> getUserAccountsById(@PathVariable Long userId) {
        log.info("Получение счетов для пользователя с ID: {}", userId);
        
        List<Account> accounts = accountRepository.findByUserIdAndActiveTrue(userId);
        List<AccountDto> accountDtos = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        log.info("Найдено {} счетов для пользователя с ID {}", accountDtos.size(), userId);
        return ResponseEntity.ok(accountDtos);
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
        return dto;
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Cash service is running");
    }
} 