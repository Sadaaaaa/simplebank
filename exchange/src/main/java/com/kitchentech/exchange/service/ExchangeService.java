package com.kitchentech.exchange.service;

import com.kitchentech.exchange.dto.ExchangeRateDto;
import com.kitchentech.exchange.entity.ExchangeRate;
import com.kitchentech.exchange.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRateRepository repository;

    public void saveRates(List<ExchangeRateDto> rates) {
        for (ExchangeRateDto dto : rates) {
            ExchangeRate rate = new ExchangeRate();
            rate.setFromCurrency(dto.getFromCurrency());
            rate.setToCurrency(dto.getToCurrency());
            rate.setRate(dto.getRate());
            rate.setTimestamp(dto.getTimestamp());
            rate.setCreatedAt(java.time.LocalDateTime.now());
            repository.save(rate);
        }
    }

    public List<ExchangeRateDto> getLatestRates() {
        // Можно доработать: возвращать только самые свежие для каждой пары
        return repository.findTop10ByOrderByTimestampDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ExchangeRateDto toDto(ExchangeRate rate) {
        ExchangeRateDto dto = new ExchangeRateDto();
        dto.setFromCurrency(rate.getFromCurrency());
        dto.setToCurrency(rate.getToCurrency());
        dto.setRate(rate.getRate());
        dto.setTimestamp(rate.getTimestamp());
        return dto;
    }
} 