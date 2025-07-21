package com.kitchentech.exchangegenerator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
@Slf4j
public class ExchangeGeneratorController {
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Exchange Generator service is running");
    }
} 