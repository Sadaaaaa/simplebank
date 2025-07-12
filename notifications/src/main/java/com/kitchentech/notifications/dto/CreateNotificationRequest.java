package com.kitchentech.notifications.dto;

import lombok.Data;

@Data
public class CreateNotificationRequest {
    private Long userId;
    private String message;
    private boolean read = false;
} 