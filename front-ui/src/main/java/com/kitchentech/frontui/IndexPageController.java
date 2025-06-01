package com.kitchentech.frontui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class IndexPageController {

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }
}
