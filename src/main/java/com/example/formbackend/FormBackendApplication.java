package com.example.formbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Dynamic Form Backend
 * 
 * This application provides REST API endpoints for handling form submissions
 * with JSON Logic-based validation.
 */
@SpringBootApplication
public class FormBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormBackendApplication.class, args);
    }
}

