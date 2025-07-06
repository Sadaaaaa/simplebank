package com.kitchentech.transfer.service;

import com.kitchentech.transfer.dto.AccountInfoDto;
import com.kitchentech.transfer.dto.TransferRequestDto;
import com.kitchentech.transfer.dto.TransferResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public TransferResponseDto performInternalTransfer(TransferRequestDto request) {
        log.info("🔄 Выполнение внутреннего перевода: {}", request);
        
        TransferResponseDto response = new TransferResponseDto();
        
        try {
            // Проверяем счета отправителя
            AccountInfoDto fromAccount = getAccountInfo(request.getFromAccountId());
            if (fromAccount == null) {
                response.setSuccess(false);
                response.setMessage("Счет отправителя не найден");
                return response;
            }

            // Проверяем счета получателя
            AccountInfoDto toAccount = getAccountInfo(request.getToAccountId());
            if (toAccount == null) {
                response.setSuccess(false);
                response.setMessage("Счет получателя не найден");
                return response;
            }

            // Проверяем, что счета принадлежат одному пользователю
            if (!fromAccount.getUserId().equals(toAccount.getUserId())) {
                response.setSuccess(false);
                response.setMessage("Перевод между разными пользователями должен выполняться через внешний перевод");
                return response;
            }

            // Проверяем достаточность средств
            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                response.setSuccess(false);
                response.setMessage("Недостаточно средств на счете отправителя");
                return response;
            }

            // Выполняем перевод через cash сервис
            boolean success = performCashOperations(fromAccount.getId(), toAccount.getId(), request.getAmount());
            
            if (success) {
                response.setSuccess(true);
                response.setMessage("Перевод выполнен успешно");
                response.setTransferId(UUID.randomUUID().toString());
                response.setFromAccountId(fromAccount.getId());
                response.setToAccountId(toAccount.getId());
                response.setAmount(request.getAmount());
                response.setTransferDate(LocalDateTime.now());
                
                // Получаем обновленные балансы
                AccountInfoDto updatedFromAccount = getAccountInfo(fromAccount.getId());
                AccountInfoDto updatedToAccount = getAccountInfo(toAccount.getId());
                response.setFromAccountNewBalance(updatedFromAccount.getBalance());
                response.setToAccountNewBalance(updatedToAccount.getBalance());
                
                log.info("✅ Внутренний перевод выполнен успешно: {}", response.getTransferId());
            } else {
                response.setSuccess(false);
                response.setMessage("Ошибка при выполнении перевода");
            }
            
        } catch (Exception e) {
            log.error("❌ Ошибка при выполнении внутреннего перевода: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Ошибка при выполнении перевода: " + e.getMessage());
        }
        
        return response;
    }

    public TransferResponseDto performExternalTransfer(TransferRequestDto request) {
        log.info("🔄 Выполнение внешнего перевода: {}", request);
        
        TransferResponseDto response = new TransferResponseDto();
        
        try {
            // Проверяем счета отправителя
            AccountInfoDto fromAccount = getAccountInfo(request.getFromAccountId());
            if (fromAccount == null) {
                response.setSuccess(false);
                response.setMessage("Счет отправителя не найден");
                return response;
            }

            // Проверяем счета получателя
            AccountInfoDto toAccount = getAccountInfo(request.getToAccountId());
            if (toAccount == null) {
                response.setSuccess(false);
                response.setMessage("Счет получателя не найден");
                return response;
            }

            // Проверяем, что счета принадлежат разным пользователям
            if (fromAccount.getUserId().equals(toAccount.getUserId())) {
                response.setSuccess(false);
                response.setMessage("Перевод между счетами одного пользователя должен выполняться через внутренний перевод");
                return response;
            }

            // Проверяем достаточность средств
            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                response.setSuccess(false);
                response.setMessage("Недостаточно средств на счете отправителя");
                return response;
            }

            // Выполняем перевод через cash сервис
            boolean success = performCashOperations(fromAccount.getId(), toAccount.getId(), request.getAmount());
            
            if (success) {
                response.setSuccess(true);
                response.setMessage("Перевод выполнен успешно");
                response.setTransferId(UUID.randomUUID().toString());
                response.setFromAccountId(fromAccount.getId());
                response.setToAccountId(toAccount.getId());
                response.setAmount(request.getAmount());
                response.setTransferDate(LocalDateTime.now());
                
                // Получаем обновленные балансы
                AccountInfoDto updatedFromAccount = getAccountInfo(fromAccount.getId());
                AccountInfoDto updatedToAccount = getAccountInfo(toAccount.getId());
                response.setFromAccountNewBalance(updatedFromAccount.getBalance());
                response.setToAccountNewBalance(updatedToAccount.getBalance());
                
                log.info("✅ Внешний перевод выполнен успешно: {}", response.getTransferId());
            } else {
                response.setSuccess(false);
                response.setMessage("Ошибка при выполнении перевода");
            }
            
        } catch (Exception e) {
            log.error("❌ Ошибка при выполнении внешнего перевода: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Ошибка при выполнении перевода: " + e.getMessage());
        }
        
        return response;
    }

    public List<AccountInfoDto> getUserAccounts(String username) {
        log.info("🔄 Получение счетов пользователя: {}", username);
        
        try {
            String url = gatewayUrl + "/api/cash/accounts/" + username;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<AccountInfoDto> accounts = java.util.Arrays.stream(response.getBody())
                        .map(item -> {
                            Map<String, Object> map = (Map<String, Object>) item;
                            AccountInfoDto dto = new AccountInfoDto();
                            dto.setId(Long.valueOf(map.get("id").toString()));
                            dto.setUserId(Long.valueOf(map.get("userId").toString()));
                            dto.setUsername((String) map.get("username"));
                            dto.setCurrency((String) map.get("currency"));
                            dto.setName((String) map.get("name"));
                            dto.setBalance(new BigDecimal(map.get("balance").toString()));
                            dto.setActive((Boolean) map.get("active"));
                            return dto;
                        })
                        .toList();
                
                log.info("✅ Получено {} счетов для пользователя {}", accounts.size(), username);
                return accounts;
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при получении счетов пользователя: {}", e.getMessage(), e);
        }
        
        return List.of();
    }

    private AccountInfoDto getAccountInfo(Long accountId) {
        try {
            String url = gatewayUrl + "/api/cash/accounts/id/" + accountId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> map = response.getBody();
                AccountInfoDto dto = new AccountInfoDto();
                dto.setId(Long.valueOf(map.get("id").toString()));
                dto.setUserId(Long.valueOf(map.get("userId").toString()));
                dto.setUsername((String) map.get("username"));
                dto.setCurrency((String) map.get("currency"));
                dto.setName((String) map.get("name"));
                dto.setBalance(new BigDecimal(map.get("balance").toString()));
                dto.setActive((Boolean) map.get("active"));
                return dto;
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при получении информации о счете {}: {}", accountId, e.getMessage(), e);
        }
        
        return null;
    }

    private boolean performCashOperations(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        try {
            // Снимаем деньги с первого счета
            Map<String, Object> withdrawData = Map.of(
                    "accountId", fromAccountId,
                    "amount", amount,
                    "operationType", "WITHDRAW"
            );

            String withdrawUrl = gatewayUrl + "/api/cash/operation";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> withdrawEntity = new HttpEntity<>(withdrawData, headers);

            ResponseEntity<Map> withdrawResponse = restTemplate.exchange(
                    withdrawUrl,
                    HttpMethod.POST,
                    withdrawEntity,
                    Map.class
            );

            if (!withdrawResponse.getStatusCode().is2xxSuccessful() || 
                !(Boolean) withdrawResponse.getBody().get("success")) {
                log.error("❌ Ошибка при снятии денег с счета {}", fromAccountId);
                return false;
            }

            // Кладем деньги на второй счет
            Map<String, Object> depositData = Map.of(
                    "accountId", toAccountId,
                    "amount", amount,
                    "operationType", "DEPOSIT"
            );

            HttpEntity<Map<String, Object>> depositEntity = new HttpEntity<>(depositData, headers);

            ResponseEntity<Map> depositResponse = restTemplate.exchange(
                    withdrawUrl,
                    HttpMethod.POST,
                    depositEntity,
                    Map.class
            );

            if (!depositResponse.getStatusCode().is2xxSuccessful() || 
                !(Boolean) depositResponse.getBody().get("success")) {
                log.error("❌ Ошибка при зачислении денег на счет {}", toAccountId);
                // TODO: Здесь нужно откатить операцию снятия
                return false;
            }

            log.info("✅ Операции снятия и зачисления выполнены успешно");
            return true;
            
        } catch (Exception e) {
            log.error("❌ Ошибка при выполнении операций с деньгами: {}", e.getMessage(), e);
            return false;
        }
    }
} 