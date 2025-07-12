package com.kitchentech.notifications.controller;

import com.kitchentech.notifications.dto.CreateNotificationRequest;
import com.kitchentech.notifications.entity.Notification;
import com.kitchentech.notifications.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/unread")
    public List<Notification> getUnread(@RequestParam Long userId) {
        return notificationService.getUnreadNotifications(userId);
    }

    @GetMapping("/all")
    public List<Notification> getAll(@RequestParam Long userId) {
        return notificationService.getAllNotifications(userId);
    }

    @PostMapping("/read/{id}")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(@RequestParam Long userId) {
        return notificationService.getUnreadNotifications(userId).size();
    }

    @PostMapping("/create")
    public Notification createNotification(@RequestBody CreateNotificationRequest request) {
        return notificationService.createNotification(request.getUserId(), request.getMessage());
    }
} 