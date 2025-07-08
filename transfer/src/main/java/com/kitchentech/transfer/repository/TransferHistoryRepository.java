package com.kitchentech.transfer.repository;

import com.kitchentech.transfer.entity.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long> {
} 