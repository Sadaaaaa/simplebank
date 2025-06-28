package com.kitchentech.frontui;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    
    @GetMapping("logout")
    public String logoutPage() {
        return "logout";
    }
    
    @PostMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            // Очищаем сессию
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        return "redirect:/logout";
    }
}
