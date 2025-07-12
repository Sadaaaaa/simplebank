package com.kitchentech.notifications.repository;

import com.kitchentech.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndReadFalse(Long userId);
    List<Notification> findByUserId(Long userId);
    long countByUserIdAndReadFalse(Long userId);
} 