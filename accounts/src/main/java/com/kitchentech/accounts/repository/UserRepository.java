package com.kitchentech.accounts.repository;

import com.kitchentech.accounts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    // Методы для работы с soft delete
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    // Методы для восстановления удаленных пользователей
    Optional<User> findByUsernameAndDeletedAtIsNotNull(String username);
    Optional<User> findByEmailAndDeletedAtIsNotNull(String email);
} 