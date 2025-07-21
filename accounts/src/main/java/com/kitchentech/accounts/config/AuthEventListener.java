package com.kitchentech.accounts.config;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthEventListener {
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        System.out.println("Успешный логин: " + event.getAuthentication().getName());
    }
}
