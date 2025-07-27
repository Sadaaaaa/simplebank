package com.kitchentech.frontui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequestMapping("/")
@Controller
@RequiredArgsConstructor
public class IndexController {

    private final RestTemplate restTemplate;
    @Value("${gateway.url}")
    private String gatewayUrl;

    @GetMapping
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("register")
    public String register() {
        return "register";
    }

    @GetMapping("register-success")
    public String registerSuccess() {
        return "register-success";
    }

    @GetMapping("logout")
    public String logout() {
        return "logout";
    }
}
