package com.kitchentech.frontui;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class IndexPageController {

    @GetMapping
    public String index() {
        // Временно всегда перенаправляем на dashboard для тестирования
        return "redirect:/dashboard";
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }
    
    @GetMapping("dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "Гость";
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            username = authentication.getName();
        }
        
        model.addAttribute("username", username);
        return "dashboard";
    }
}
