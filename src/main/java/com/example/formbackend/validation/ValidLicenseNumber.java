package com.example.formbackend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for license number
 * 
 * Validates that the license number is 6-12 uppercase letters or numbers.
 */
@Documented
@Constraint(validatedBy = LicenseNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLicenseNumber {
    String message() default "License number must be 6-12 uppercase letters or numbers";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

