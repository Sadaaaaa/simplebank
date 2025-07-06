package com.kitchentech.exchange.repository;

import com.kitchentech.exchange.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    List<ExchangeRate> findTop1ByFromCurrencyAndToCurrencyOrderByTimestampDesc(String fromCurrency, String toCurrency);
    List<ExchangeRate> findTop10ByOrderByTimestampDesc();
    List<ExchangeRate> findByTimestampAfter(java.time.LocalDateTime after);
} 