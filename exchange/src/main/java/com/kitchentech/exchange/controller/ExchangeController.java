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

    @PostMapping("/rates")
    public ResponseEntity<Void> updateRates(@RequestBody List<ExchangeRateDto> rates) {
        exchangeService.updateRates(rates);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rates")
    public ResponseEntity<List<ExchangeRateDto>> getLatestRates() {
        return ResponseEntity.ok(exchangeService.getLatestRates());
    }
} 