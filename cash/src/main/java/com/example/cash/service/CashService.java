package com.example.cash.service;

import com.example.cash.dto.CashOperationDto;
import com.example.cash.dto.CashOperationResponseDto;
import com.example.cash.entity.Account;
import com.example.cash.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {
    
    private final AccountRepository accountRepository;
    
    @Transactional
    public CashOperationResponseDto performOperation(CashOperationDto operationDto) {
        log.info("Выполняется операция: {}", operationDto);
        
        CashOperationResponseDto response = new CashOperationResponseDto();
        
        try {
            Account account = accountRepository.findByIdAndActiveTrue(operationDto.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Счет не найден или неактивен"));
            
            BigDecimal currentBalance = account.getBalance();
            BigDecimal amount = operationDto.getAmount();
            
            if ("WITHDRAW".equals(operationDto.getOperationType())) {
                if (currentBalance.compareTo(amount) < 0) {
                    response.setSuccess(false);
                    response.setMessage("Недостаточно средств на счете. Доступно: " + currentBalance);
                    response.setNewBalance(currentBalance);
                    response.setAccountId(account.getId());
                    log.warn("Попытка снятия суммы {} с счета {}, но доступно только {}", 
                            amount, account.getId(), currentBalance);
                    return response;
                }
                
                account.setBalance(currentBalance.subtract(amount));
                log.info("Снято {} с счета {}. Новый баланс: {}", amount, account.getId(), account.getBalance());
            } else if ("DEPOSIT".equals(operationDto.getOperationType())) {
                account.setBalance(currentBalance.add(amount));
                log.info("Внесено {} на счет {}. Новый баланс: {}", amount, account.getId(), account.getBalance());
            } else {
                throw new RuntimeException("Неизвестный тип операции: " + operationDto.getOperationType());
            }
            
            accountRepository.save(account);
            
            response.setSuccess(true);
            response.setMessage("Операция выполнена успешно");
            response.setNewBalance(account.getBalance());
            response.setAccountId(account.getId());
            
        } catch (Exception e) {
            log.error("Ошибка при выполнении операции: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Ошибка: " + e.getMessage());
        }
        
        return response;
    }
} 