package com.example.cash.repository;

import com.example.cash.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIdAndActiveTrue(Long id);
    List<Account> findByUsernameAndDeletedAtIsNull(String username);
    List<Account> findByUserIdAndDeletedAtIsNull(Long userId);
    Optional<Account> findByIdAndDeletedAtIsNull(Long id);
}