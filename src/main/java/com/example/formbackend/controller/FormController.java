package com.example.formbackend.controller;

import com.example.formbackend.dto.FormSubmissionDTO;
import com.example.formbackend.model.FormConfig;
import com.example.formbackend.service.FormValidationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for handling form submissions
 *
 * Provides endpoints for:
 * - Retrieving form configuration
 * - Submitting form data with validation
 * - Handling validation errors
 */
@RestController
@RequestMapping("/forms")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class FormController {

    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    @Autowired
    private FormValidationService validationService;

    /**
     * Submit form data
     * 
     * @param formData The form submission data
     * @return Response with success message or validation errors
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitForm(@Valid @RequestBody FormSubmissionDTO formData) {
        logger.info("Received form submission: {}", formData);
        
        // Perform JSON Logic validation
        List<String> jsonLogicErrors = validationService.validateFormSubmission(formData);
        
        if (!jsonLogicErrors.isEmpty()) {
            logger.warn("JSON Logic validation failed: {}", jsonLogicErrors);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Validation failed");
            errorResponse.put("errors", jsonLogicErrors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // If validation passes, process the form (e.g., save to database)
        logger.info("Form validation successful. Processing submission...");
        
        // TODO: Add your business logic here (e.g., save to database, send email, etc.)
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Form submitted successfully!");
        response.put("data", formData);
        response.put("submittedAt", new Date());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get form configuration
     *
     * @return The form configuration JSON
     */
    @GetMapping("/config")
    public ResponseEntity<FormConfig> getFormConfig() {
        logger.info("Retrieving form configuration");
        FormConfig config = validationService.getFormConfig();
        return ResponseEntity.ok(config);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Form Backend API");
        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler for validation errors
     * 
     * Catches Bean Validation errors and returns them in a structured format
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        logger.warn("Bean validation failed: {}", fieldErrors);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Validation failed");
        response.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Generic exception handler
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "An unexpected error occurred");
        response.put("error", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

