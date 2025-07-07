package com.kitchentech.exchange.service;

import com.kitchentech.exchange.dto.ExchangeRateDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeService {
    // Ключ: from-to (например, USD_EUR), значение: последний курс
    private final Map<String, ExchangeRateDto> latestRates = new ConcurrentHashMap<>();

    public void updateRates(List<ExchangeRateDto> rates) {
        for (ExchangeRateDto rate : rates) {
            String key = rate.getFromCurrency() + "_" + rate.getToCurrency();
            latestRates.put(key, rate);
        }
    }

    public List<ExchangeRateDto> getLatestRates() {
        return new ArrayList<>(latestRates.values());
    }
} 