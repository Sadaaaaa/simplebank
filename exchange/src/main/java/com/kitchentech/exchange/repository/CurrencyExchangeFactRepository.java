package com.kitchentech.exchange.repository;

import com.kitchentech.exchange.entity.CurrencyExchangeFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyExchangeFactRepository extends JpaRepository<CurrencyExchangeFact, Long> {
} 