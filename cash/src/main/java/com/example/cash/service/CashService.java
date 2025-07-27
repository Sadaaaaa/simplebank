package com.example.cash.service;

import com.example.cash.dto.CashOperationDto;
import com.example.cash.dto.CashOperationResponseDto;
import com.example.cash.entity.Account;
import com.example.cash.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {
    
    private final AccountRepository accountRepository;
    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    
    @Value("${gateway.url:http://localhost:8081}")
    private String gatewayUrl;
    
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
                
                // Отправляем уведомление о снятии
                sendNotification(account.getUserId(), 
                    String.format("Снято %.2f %s со счета %s", amount, account.getCurrency(), account.getName()));
                
            } else if ("DEPOSIT".equals(operationDto.getOperationType())) {
                account.setBalance(currentBalance.add(amount));
                log.info("Внесено {} на счет {}. Новый баланс: {}", amount, account.getId(), account.getBalance());
                
                // Отправляем уведомление о пополнении
                sendNotification(account.getUserId(), 
                    String.format("Внесено %.2f %s на счет %s", amount, account.getCurrency(), account.getName()));
                
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
    
    private void sendNotification(Long userId, String message) {
        try {
            Map<String, Object> notification = Map.of(
                "userId", userId,
                "message", message,
                "read", false
            );
            String url = gatewayUrl + "/api/notifications/create";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notification, headers);
            restTemplate.postForEntity(url, entity, Void.class);
            log.info("✅ Уведомление отправлено пользователю {}: {}", userId, message);
        } catch (Exception e) {
            log.error("❌ Не удалось отправить уведомление пользователю {}: {}", userId, e.getMessage(), e);
        }
    }

    private String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("auth-server")
                .principal("cash-service")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            log.error("❌ Не удалось получить токен доступа");
            return "";
        }

        log.info("[OAUTH2] Получен токен у auth-server: {}... ", authorizedClient.getAccessToken().getTokenValue());
        return authorizedClient.getAccessToken().getTokenValue();
    }
} 