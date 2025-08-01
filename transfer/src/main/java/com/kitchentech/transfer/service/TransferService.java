package com.kitchentech.transfer.service;

import com.kitchentech.transfer.dto.AccountInfoDto;
import com.kitchentech.transfer.dto.TransferRequestDto;
import com.kitchentech.transfer.dto.TransferResponseDto;
import com.kitchentech.transfer.entity.TransferHistory;
import com.kitchentech.transfer.repository.TransferHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
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
    private final TransferHistoryRepository historyRepository;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Value("${gateway.url}")
    private String gatewayUrl;

    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        try {
            String url = gatewayUrl + "/api/exchange/rates";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> rates = response.getBody();
                for (Map<String, Object> rate : rates) {
                    if (fromCurrency.equals(rate.get("fromCurrency")) && toCurrency.equals(rate.get("toCurrency"))) {
                        return new BigDecimal(rate.get("rate").toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при получении курса валют: {} -> {}: {}", fromCurrency, toCurrency, e.getMessage(), e);
        }
        throw new RuntimeException("Не удалось получить курс валют для " + fromCurrency + " -> " + toCurrency);
    }

    public TransferResponseDto performInternalTransfer(TransferRequestDto request) {
        log.info("🔄 Выполнение внутреннего перевода: {}", request);
        TransferResponseDto response = new TransferResponseDto();
        try {
            AccountInfoDto fromAccount = getAccountInfo(request.getFromAccountId());
            if (fromAccount == null) {
                response.setSuccess(false);
                response.setMessage("Счет отправителя не найден");
                return response;
            }
            AccountInfoDto toAccount = getAccountInfo(request.getToAccountId());
            if (toAccount == null) {
                response.setSuccess(false);
                response.setMessage("Счет получателя не найден");
                return response;
            }
            if (!fromAccount.getUserId().equals(toAccount.getUserId())) {
                response.setSuccess(false);
                response.setMessage("Перевод между разными пользователями должен выполняться через внешний перевод");
                return response;
            }
            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                response.setSuccess(false);
                response.setMessage("Недостаточно средств на счете отправителя");
                return response;
            }
            BigDecimal amountToWithdraw = request.getAmount();
            BigDecimal amountToDeposit = amountToWithdraw;

            if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
                BigDecimal rate = getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency());
                amountToDeposit = amountToWithdraw.multiply(rate);
                log.info("💱 Перевод с конвертацией: {} {} -> {} {} по курсу {}", amountToWithdraw, fromAccount.getCurrency(), amountToDeposit, toAccount.getCurrency(), rate);
            }
            boolean success = performCashOperations(fromAccount.getId(), toAccount.getId(), amountToWithdraw, amountToDeposit);
            if (success) {
                response.setSuccess(true);
                response.setMessage("Перевод выполнен успешно");
                response.setTransferId(UUID.randomUUID().toString());
                response.setFromAccountId(fromAccount.getId());
                response.setToAccountId(toAccount.getId());
                response.setAmount(request.getAmount());
                response.setTransferDate(LocalDateTime.now());
                AccountInfoDto updatedFromAccount = getAccountInfo(fromAccount.getId());
                AccountInfoDto updatedToAccount = getAccountInfo(toAccount.getId());
                response.setFromAccountNewBalance(updatedFromAccount.getBalance());
                response.setToAccountNewBalance(updatedToAccount.getBalance());
                log.info("✅ Внутренний перевод выполнен успешно: {}", response.getTransferId());
                sendExchangeFact(
                    fromAccount.getUserId(),
                    fromAccount.getCurrency(),
                    toAccount.getCurrency(),
                    amountToWithdraw,
                    amountToDeposit,
                    (!fromAccount.getCurrency().equals(toAccount.getCurrency()) ? getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency()) : BigDecimal.ONE),
                    true
                );

                String fromMessage = String.format("Переведено %.2f %s со счета %s на счет %s", 
                    amountToWithdraw, fromAccount.getCurrency(), fromAccount.getName(), toAccount.getName());
                String toMessage = String.format("Получено %.2f %s на счет %s со счета %s", 
                    amountToDeposit, toAccount.getCurrency(), toAccount.getName(), fromAccount.getName());
                
                sendNotification(fromAccount.getUserId(), fromMessage);
                sendNotification(toAccount.getUserId(), toMessage);
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
            AccountInfoDto fromAccount = getAccountInfo(request.getFromAccountId());
            if (fromAccount == null) {
                response.setSuccess(false);
                response.setMessage("Счет отправителя не найден");
                return response;
            }
            AccountInfoDto toAccount = getAccountInfo(request.getToAccountId());
            if (toAccount == null) {
                response.setSuccess(false);
                response.setMessage("Счет получателя не найден");
                return response;
            }
            if (fromAccount.getUserId().equals(toAccount.getUserId())) {
                response.setSuccess(false);
                response.setMessage("Перевод между счетами одного пользователя должен выполняться через внутренний перевод");
                return response;
            }
            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                response.setSuccess(false);
                response.setMessage("Недостаточно средств на счете отправителя");
                return response;
            }
            // 1. Проверка через blocker
            boolean allowed = true;
            String blockReason = "";
            try {
                Map<String, Object> blockReq = Map.of(
                    "fromUserId", fromAccount.getUserId(),
                    "toUserId", toAccount.getUserId(),
                    "amount", request.getAmount(),
                    "currency", fromAccount.getCurrency()
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(getAccessToken());
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(blockReq, headers);
                ResponseEntity<Map> blockResp = restTemplate.exchange(
                    gatewayUrl + "/api/blocker/check-transfer",
                    HttpMethod.POST,
                    entity,
                    Map.class
                );
                if (blockResp.getStatusCode().is2xxSuccessful() && blockResp.getBody() != null) {
                    allowed = Boolean.TRUE.equals(blockResp.getBody().get("allowed"));
                    blockReason = (String) blockResp.getBody().get("reason");
                }
            } catch (Exception e) {
                log.error("❌ Ошибка при проверке через blocker: {}", e.getMessage(), e);
                allowed = false;
                blockReason = "Ошибка связи с сервисом blocker";
            }
            // 2. Сохраняем историю
            BigDecimal amountToWithdraw = request.getAmount();
            BigDecimal amountToDeposit = amountToWithdraw;
            BigDecimal rate = BigDecimal.ONE;
            if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
                rate = getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency());
                amountToDeposit = amountToWithdraw.multiply(rate);
            }
            TransferHistory history = new TransferHistory();
            history.setFromUserId(fromAccount.getUserId());
            history.setToUserId(toAccount.getUserId());
            history.setFromAccountId(fromAccount.getId());
            history.setToAccountId(toAccount.getId());
            history.setFromCurrency(fromAccount.getCurrency());
            history.setToCurrency(toAccount.getCurrency());
            history.setAmountFrom(amountToWithdraw);
            history.setAmountTo(amountToDeposit);
            history.setRate(rate);
            history.setDate(LocalDateTime.now());
            history.setAllowed(allowed);
            history.setBlockReason(blockReason);
            history.setInternal(false);
            historyRepository.save(history);
            // 3. Если не разрешено — возвращаем ошибку
            if (!allowed) {
                response.setSuccess(false);
                response.setMessage("Перевод заблокирован: " + blockReason);
                return response;
            }
            // 4. Выполняем перевод через cash сервис
            boolean success = performCashOperations(fromAccount.getId(), toAccount.getId(), amountToWithdraw, amountToDeposit);
            if (success) {
                response.setSuccess(true);
                response.setMessage("Перевод выполнен успешно");
                response.setTransferId(UUID.randomUUID().toString());
                response.setFromAccountId(fromAccount.getId());
                response.setToAccountId(toAccount.getId());
                response.setAmount(request.getAmount());
                response.setTransferDate(LocalDateTime.now());
                AccountInfoDto updatedFromAccount = getAccountInfo(fromAccount.getId());
                AccountInfoDto updatedToAccount = getAccountInfo(toAccount.getId());
                response.setFromAccountNewBalance(updatedFromAccount.getBalance());
                response.setToAccountNewBalance(updatedToAccount.getBalance());
                log.info("✅ Внешний перевод выполнен успешно: {}", response.getTransferId());
                sendExchangeFact(
                    fromAccount.getUserId(),
                    fromAccount.getCurrency(),
                    toAccount.getCurrency(),
                    amountToWithdraw,
                    amountToDeposit,
                    rate,
                    false // external
                );
                
                // Отправляем уведомления
                String fromMessage = String.format("Списано %.2f %s со счета %s", amountToWithdraw, fromAccount.getCurrency(), fromAccount.getName());
                String toMessage = String.format("Зачислено %.2f %s на счет %s", amountToDeposit, toAccount.getCurrency(), toAccount.getName());
                
                sendNotification(fromAccount.getUserId(), fromMessage);
                sendNotification(toAccount.getUserId(), toMessage);
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
            headers.setBearerAuth(getAccessToken());
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
            headers.setBearerAuth(getAccessToken());
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

    // Новый метод для cash операций с разными суммами
    private boolean performCashOperations(Long fromAccountId, Long toAccountId, BigDecimal amountToWithdraw, BigDecimal amountToDeposit) {
        try {
            // Снимаем деньги с первого счета
            Map<String, Object> withdrawData = Map.of(
                    "accountId", fromAccountId,
                    "amount", amountToWithdraw,
                    "operationType", "WITHDRAW"
            );
            String withdrawUrl = gatewayUrl + "/api/cash/operation";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());
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
                    "amount", amountToDeposit,
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

    private void sendExchangeFact(Long userId, String fromCurrency, String toCurrency, BigDecimal amountFrom, BigDecimal amountTo, BigDecimal rate, boolean internal) {
        try {
            Map<String, Object> fact = Map.of(
                "userId", userId,
                "fromCurrency", fromCurrency,
                "toCurrency", toCurrency,
                "amountFrom", amountFrom,
                "amountTo", amountTo,
                "rate", rate,
                "date", LocalDateTime.now().toString(),
                "internal", internal
            );
            String url = gatewayUrl + "/api/exchange/convert";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(fact, headers);
            restTemplate.postForEntity(url, entity, Void.class);
        } catch (Exception e) {
            log.error("❌ Не удалось отправить факт обмена валюты: {}", e.getMessage(), e);
        }
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
                .principal("transfer-service")
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