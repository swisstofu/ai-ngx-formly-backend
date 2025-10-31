package com.example.formbackend.service;

import com.example.formbackend.dto.FormSubmissionDTO;
import com.example.formbackend.model.FormConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FormValidationService
 * 
 * Tests the configuration-based JSON Logic validation
 */
@SpringBootTest
class FormValidationServiceTest {

    @Autowired
    private FormValidationService validationService;

    private FormSubmissionDTO validDto;

    @BeforeEach
    void setUp() {
        // Create a valid DTO for testing
        validDto = new FormSubmissionDTO();
        validDto.setFirstName("John");
        validDto.setLastName("Doe");
        validDto.setEmail("john.doe@example.com");
        validDto.setCountry("ca"); // Canada - no age required
    }

    @Test
    void testFormConfigLoaded() {
        // Verify that form configuration was loaded successfully
        FormConfig config = validationService.getFormConfig();
        
        assertNotNull(config, "Form configuration should be loaded");
        assertNotNull(config.getFields(), "Form fields should not be null");
        assertFalse(config.getFields().isEmpty(), "Form fields should not be empty");
        
        // Verify expected fields are present
        assertTrue(config.getFields().stream()
                .anyMatch(f -> "firstName".equals(f.getKey())), 
                "firstName field should be present");
        assertTrue(config.getFields().stream()
                .anyMatch(f -> "age".equals(f.getKey())), 
                "age field should be present");
        assertTrue(config.getFields().stream()
                .anyMatch(f -> "licenseNumber".equals(f.getKey())), 
                "licenseNumber field should be present");
    }

    @Test
    void testValidFormSubmission() {
        // Valid form for Canada (no age required)
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "Valid form should have no errors");
    }

    @Test
    void testAgeRequiredForUSResidents() {
        // US resident without age should fail
        validDto.setCountry("us");
        validDto.setAge(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertFalse(errors.isEmpty(), "Should have validation errors");
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("Age") && e.contains("US")),
                "Should have age required error for US residents");
    }

    @Test
    void testAgeProvidedForUSResidents() {
        // US resident with age should pass
        validDto.setCountry("us");
        validDto.setAge(25);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "Valid US form with age should have no errors");
    }

    @Test
    void testAgeNotRequiredForNonUSResidents() {
        // Non-US resident without age should pass
        validDto.setCountry("ca");
        validDto.setAge(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "Age should not be required for non-US residents");
    }

    @Test
    void testLicenseNumberRequiredWhenHasLicense() {
        // Has license but no license number should fail
        validDto.setAge(25);
        validDto.setHasLicense(true);
        validDto.setLicenseNumber(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertFalse(errors.isEmpty(), "Should have validation errors");
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("License number") || e.contains("license")),
                "Should have license number required error");
    }

    @Test
    void testLicenseNumberProvidedWhenHasLicense() {
        // Has license with license number should pass
        validDto.setAge(25);
        validDto.setHasLicense(true);
        validDto.setLicenseNumber("ABC12345");
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "Valid form with license should have no errors");
    }

    @Test
    void testLicenseNumberNotRequiredWhenNoLicense() {
        // No license and no license number should pass
        validDto.setAge(25);
        validDto.setHasLicense(false);
        validDto.setLicenseNumber(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "License number should not be required when hasLicense is false");
    }

    @Test
    void testCompanyNameRequiredForEmployed() {
        // Employed without company name should fail
        validDto.setAge(25);
        validDto.setEmploymentStatus("employed");
        validDto.setCompanyName(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertFalse(errors.isEmpty(), "Should have validation errors");
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("Company") || e.contains("company")),
                "Should have company name required error");
    }

    @Test
    void testCompanyNameRequiredForSelfEmployed() {
        // Self-employed without company name should fail
        validDto.setAge(25);
        validDto.setEmploymentStatus("self-employed");
        validDto.setCompanyName(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertFalse(errors.isEmpty(), "Should have validation errors");
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("Company") || e.contains("company")),
                "Should have company name required error");
    }

    @Test
    void testCompanyNameProvidedForEmployed() {
        // Employed with company name and annual income should pass
        validDto.setAge(25);
        validDto.setEmploymentStatus("employed");
        validDto.setCompanyName("Acme Corp");
        validDto.setAnnualIncome(50000.0); // Required for employed adults

        List<String> errors = validationService.validateFormSubmission(validDto);

        assertTrue(errors.isEmpty(), "Valid employed form should have no errors");
    }

    @Test
    void testCompanyNameNotRequiredForUnemployed() {
        // Unemployed without company name should pass
        validDto.setAge(25);
        validDto.setEmploymentStatus("unemployed");
        validDto.setCompanyName(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "Company name should not be required for unemployed");
    }

    @Test
    void testAnnualIncomeRequiredForEmployedAdult() {
        // Employed adult without annual income should fail
        validDto.setAge(25);
        validDto.setEmploymentStatus("employed");
        validDto.setCompanyName("Acme Corp");
        validDto.setAnnualIncome(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertFalse(errors.isEmpty(), "Should have validation errors");
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("income") || e.contains("Income")),
                "Should have annual income required error");
    }

    @Test
    void testAnnualIncomeRequiredForRetiredAdult() {
        // Retired adult without annual income should fail
        validDto.setAge(65);
        validDto.setEmploymentStatus("retired");
        validDto.setAnnualIncome(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertFalse(errors.isEmpty(), "Should have validation errors");
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("income") || e.contains("Income")),
                "Should have annual income required error");
    }

    @Test
    void testAnnualIncomeProvidedForEmployedAdult() {
        // Employed adult with annual income should pass
        validDto.setAge(25);
        validDto.setEmploymentStatus("employed");
        validDto.setCompanyName("Acme Corp");
        validDto.setAnnualIncome(50000.0);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "Valid employed adult form should have no errors");
    }

    @Test
    void testAnnualIncomeNotRequiredForStudent() {
        // Student without annual income should pass
        validDto.setAge(20);
        validDto.setEmploymentStatus("student");
        validDto.setAnnualIncome(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "Annual income should not be required for students");
    }

    @Test
    void testMultipleValidationErrors() {
        // US resident without age, has license without number
        validDto.setCountry("us");
        validDto.setAge(null);
        validDto.setHasLicense(true);
        validDto.setLicenseNumber(null);
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertFalse(errors.isEmpty(), "Should have multiple validation errors");
        assertTrue(errors.size() >= 2, "Should have at least 2 errors");
    }

    @Test
    void testComplexValidForm() {
        // Complex valid form with all fields
        validDto.setCountry("us");
        validDto.setAge(30);
        validDto.setHasLicense(true);
        validDto.setLicenseNumber("ABC12345");
        validDto.setEmploymentStatus("employed");
        validDto.setCompanyName("Tech Corp");
        validDto.setAnnualIncome(75000.0);
        validDto.setComments("Test comments");
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertTrue(errors.isEmpty(), "Complex valid form should have no errors");
    }

    @Test
    void testEmptyStringTreatedAsNull() {
        // Empty string should be treated as missing value
        validDto.setCountry("us");
        validDto.setAge(25);
        validDto.setHasLicense(true);
        validDto.setLicenseNumber(""); // Empty string
        
        List<String> errors = validationService.validateFormSubmission(validDto);
        
        assertFalse(errors.isEmpty(), "Empty string should be treated as missing value");
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("License number") || e.contains("license")),
                "Should have license number required error");
    }
}

