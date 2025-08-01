package com.kitchentech.exchangegenerator.service;

import com.kitchentech.exchangegenerator.dto.ExchangeRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratorService {

    private static final List<String> CURRENCIES = Arrays.asList("RUB", "USD", "EUR");
    private static final String BASE_CURRENCY = "RUB";
    
    private static final Map<String, BigDecimal> BASE_RATES = Map.of(
        "USD", new BigDecimal("0.011"), // 1 RUB = 0.011 USD
        "EUR", new BigDecimal("0.010")  // 1 RUB = 0.010 EUR
    );

    private BigDecimal generateRate(String fromCurrency, String toCurrency) {

        if (fromCurrency.equals(BASE_CURRENCY)) {
            return getBaseRate(toCurrency);
        } else if (toCurrency.equals(BASE_CURRENCY)) {
            return BigDecimal.ONE.divide(getBaseRate(fromCurrency), 4, RoundingMode.HALF_UP);
        } else {

            BigDecimal fromToRub = BigDecimal.ONE.divide(getBaseRate(fromCurrency), 4, RoundingMode.HALF_UP);
            BigDecimal rubToTo = getBaseRate(toCurrency);
            return fromToRub.multiply(rubToTo).setScale(4, RoundingMode.HALF_UP);
        }
    }

    public List<ExchangeRateDto> generateExchangeRates() {
        List<ExchangeRateDto> rates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (String fromCurrency : CURRENCIES) {
            for (String toCurrency : CURRENCIES) {
                if (!fromCurrency.equals(toCurrency)) {
                    BigDecimal rate = generateRate(fromCurrency, toCurrency);
                    
                    ExchangeRateDto rateDto = new ExchangeRateDto();
                    rateDto.setFromCurrency(fromCurrency);
                    rateDto.setToCurrency(toCurrency);
                    rateDto.setRate(rate);
                    rateDto.setTimestamp(now);
                    
                    rates.add(rateDto);
                }
            }
        }
        
        return rates;
    }

    private BigDecimal getBaseRate(String currency) {
        BigDecimal baseRate = BASE_RATES.get(currency);
        if (baseRate == null) {
            return BigDecimal.ONE;
        }
        
        double randomChange = 0.95 + (Math.random() * 0.1); // 0.95 - 1.05
        return baseRate.multiply(BigDecimal.valueOf(randomChange)).setScale(4, RoundingMode.HALF_UP);
    }
}