package com.example.cash.repository;

import com.example.cash.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUsernameAndActiveTrue(String username);
    List<Account> findByUserIdAndActiveTrue(Long userId);
    Optional<Account> findByIdAndActiveTrue(Long id);
    
    // Методы для работы с soft delete
    List<Account> findByUsernameAndDeletedAtIsNull(String username);
    List<Account> findByUserIdAndDeletedAtIsNull(Long userId);
    Optional<Account> findByIdAndDeletedAtIsNull(Long id);
    
    // Методы для восстановления удаленных счетов
    List<Account> findByUsernameAndDeletedAtIsNotNull(String username);
    List<Account> findByUserIdAndDeletedAtIsNotNull(Long userId);
} 