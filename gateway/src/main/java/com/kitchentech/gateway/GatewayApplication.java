package com.kitchentech.gateway;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public CommandLineRunner testRoutes(RouteLocator locator) {
        return args -> {
            locator.getRoutes().subscribe(route -> System.out.println("✅ Route: " + route.getId()));
        };
    }

}
