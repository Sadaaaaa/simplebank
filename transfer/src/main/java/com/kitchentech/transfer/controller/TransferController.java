package com.kitchentech.transfer.controller;

import com.kitchentech.transfer.dto.AccountInfoDto;
import com.kitchentech.transfer.dto.TransferRequestDto;
import com.kitchentech.transfer.dto.TransferResponseDto;
import com.kitchentech.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/internal")
    public ResponseEntity<TransferResponseDto> performInternalTransfer(@RequestBody TransferRequestDto request) {
        log.info("🔄 Запрос на внутренний перевод: {}", request);
        TransferResponseDto response = transferService.performInternalTransfer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/external")
    public ResponseEntity<TransferResponseDto> performExternalTransfer(@RequestBody TransferRequestDto request) {
        log.info("🔄 Запрос на внешний перевод: {}", request);
        TransferResponseDto response = transferService.performExternalTransfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{username}")
    public ResponseEntity<List<AccountInfoDto>> getUserAccounts(@PathVariable String username) {
        log.info("🔄 Запрос на получение счетов пользователя: {}", username);
        List<AccountInfoDto> accounts = transferService.getUserAccounts(username);
        return ResponseEntity.ok(accounts);
    }
} 