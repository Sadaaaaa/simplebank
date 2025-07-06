package com.kitchentech.exchange.controller;

import com.kitchentech.exchange.dto.ExchangeRateDto;
import com.kitchentech.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {
    private final ExchangeService exchangeService;

    // Получение актуальных курсов для фронта
    @GetMapping("/rates")
    public ResponseEntity<List<ExchangeRateDto>> getRates() {
        return ResponseEntity.ok(exchangeService.getLatestRates());
    }

    // Приём новых курсов от генератора
    @PostMapping("/rates")
    public ResponseEntity<Void> saveRates(@RequestBody List<ExchangeRateDto> rates) {
        exchangeService.saveRates(rates);
        return ResponseEntity.ok().build();
    }
} 