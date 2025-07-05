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
} 