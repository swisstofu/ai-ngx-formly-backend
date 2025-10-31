package com.example.formbackend.dto;

import com.example.formbackend.validation.ValidLicenseNumber;
import com.example.formbackend.validation.ValidName;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for form submission
 * 
 * This DTO matches the frontend form structure and includes
 * Bean Validation annotations for server-side validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmissionDTO {

    /**
     * First name - required, min 2 characters, letters/spaces/hyphens/apostrophes only
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, message = "First name must be at least 2 characters")
    @ValidName(message = "Only letters, spaces, hyphens, and apostrophes are allowed")
    @JsonProperty("firstName")
    private String firstName;

    /**
     * Last name - required, min 2 characters, letters/spaces/hyphens/apostrophes only
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, message = "Last name must be at least 2 characters")
    @ValidName(message = "Only letters, spaces, hyphens, and apostrophes are allowed")
    @JsonProperty("lastName")
    private String lastName;

    /**
     * Email - required, valid email format
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Please enter a valid email address")
    @JsonProperty("email")
    private String email;

    /**
     * Country - required
     */
    @NotBlank(message = "Please select a country")
    @JsonProperty("country")
    private String country;

    /**
     * State/Province - optional, shown only for US/Canada
     */
    @JsonProperty("state")
    private String state;

    /**
     * Age - conditionally required for US residents, 0-120
     */
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 120, message = "Age must be less than 120")
    @JsonProperty("age")
    private Integer age;

    /**
     * Has driver's license - boolean, shown only if age >= 16
     */
    @JsonProperty("hasLicense")
    private Boolean hasLicense;

    /**
     * License number - required if hasLicense is true, 6-12 uppercase letters/numbers
     */
    @ValidLicenseNumber(message = "License number must be 6-12 uppercase letters or numbers")
    @JsonProperty("licenseNumber")
    private String licenseNumber;

    /**
     * Employment status - optional, disabled if age < 18
     */
    @JsonProperty("employmentStatus")
    private String employmentStatus;

    /**
     * Company name - required if employed or self-employed, min 2 characters
     */
    @Size(min = 2, message = "Company name must be at least 2 characters")
    @JsonProperty("companyName")
    private String companyName;

    /**
     * Annual income - required if employed/self-employed/retired and age >= 18, min 0
     */
    @Min(value = 0, message = "Annual income must be at least 0")
    @JsonProperty("annualIncome")
    private Double annualIncome;

    /**
     * Additional comments - optional
     */
    @JsonProperty("comments")
    private String comments;
}

