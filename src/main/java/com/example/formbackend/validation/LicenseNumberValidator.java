package com.example.formbackend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator implementation for ValidLicenseNumber annotation
 * 
 * Validates that the license number is 6-12 uppercase letters or numbers.
 */
public class LicenseNumberValidator implements ConstraintValidator<ValidLicenseNumber, String> {

    private static final Pattern LICENSE_PATTERN = Pattern.compile("^[A-Z0-9]{6,12}$");

    @Override
    public void initialize(ValidLicenseNumber constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null or empty values are considered valid (use @NotNull/@NotBlank for null checks)
        if (value == null || value.isEmpty()) {
            return true;
        }
        
        return LICENSE_PATTERN.matcher(value).matches();
    }
}

