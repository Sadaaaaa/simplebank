package com.kitchentech.exchange.service;

import com.kitchentech.exchange.dto.CurrencyExchangeFactDto;
import com.kitchentech.exchange.dto.ExchangeRateDto;
import com.kitchentech.exchange.entity.CurrencyExchangeFact;
import com.kitchentech.exchange.repository.CurrencyExchangeFactRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeService {
    // Ключ: from-to (например, USD_EUR), значение: последний курс
    private final Map<String, ExchangeRateDto> latestRates = new ConcurrentHashMap<>();
    private final CurrencyExchangeFactRepository factRepository;

    public ExchangeService(CurrencyExchangeFactRepository factRepository) {
        this.factRepository = factRepository;
    }

    public void updateRates(List<ExchangeRateDto> rates) {
        for (ExchangeRateDto rate : rates) {
            String key = rate.getFromCurrency() + "_" + rate.getToCurrency();
            latestRates.put(key, rate);
        }
    }

    public List<ExchangeRateDto> getLatestRates() {
        return new ArrayList<>(latestRates.values());
    }

    public void saveExchangeFact(CurrencyExchangeFactDto dto) {
        CurrencyExchangeFact fact = new CurrencyExchangeFact();
        fact.setUserId(dto.getUserId());
        fact.setFromCurrency(dto.getFromCurrency());
        fact.setToCurrency(dto.getToCurrency());
        fact.setAmountFrom(dto.getAmountFrom());
        fact.setAmountTo(dto.getAmountTo());
        fact.setRate(dto.getRate());
        fact.setDate(dto.getDate());
        fact.setInternal(dto.isInternal());
        factRepository.save(fact);
    }
} 