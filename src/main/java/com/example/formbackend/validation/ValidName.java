package com.example.formbackend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for name fields
 * 
 * Validates that the name contains only letters, spaces, hyphens, and apostrophes.
 * Supports international characters (À-ÿ).
 */
@Documented
@Constraint(validatedBy = NameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {
    String message() default "Only letters, spaces, hyphens, and apostrophes are allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

