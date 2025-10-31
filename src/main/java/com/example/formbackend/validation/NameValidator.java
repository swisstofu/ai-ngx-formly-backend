package com.example.formbackend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator implementation for ValidName annotation
 * 
 * Validates that the name contains only letters, spaces, hyphens, and apostrophes.
 * Supports international characters (À-ÿ).
 */
public class NameValidator implements ConstraintValidator<ValidName, String> {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'\\-]+$");

    @Override
    public void initialize(ValidName constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are considered valid (use @NotNull/@NotBlank for null checks)
        if (value == null || value.isEmpty()) {
            return true;
        }
        
        return NAME_PATTERN.matcher(value).matches();
    }
}

