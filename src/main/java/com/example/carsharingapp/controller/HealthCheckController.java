package com.example.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check", description = "Endpoints for application health monitoring")
@RestController
public class HealthCheckController {

    @Operation(summary = "Check application health")
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
