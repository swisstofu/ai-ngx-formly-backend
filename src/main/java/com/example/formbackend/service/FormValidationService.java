package com.example.formbackend.service;

import com.example.formbackend.dto.FormSubmissionDTO;
import com.example.formbackend.model.FieldConfig;
import com.example.formbackend.model.FormConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jamsesso.jsonlogic.JsonLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * Service for validating form submissions using JSON Logic
 *
 * This service loads the form-config.json file and applies the same
 * JSON Logic rules used in the frontend to validate conditional
 * requirements on the backend.
 */
@Service
public class FormValidationService {

    private static final Logger logger = LoggerFactory.getLogger(FormValidationService.class);

    private final JsonLogic jsonLogic;
    private final ObjectMapper objectMapper;
    private FormConfig formConfig;

    public FormValidationService() {
        this.jsonLogic = new JsonLogic();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Load form configuration from JSON file on startup
     */
    @PostConstruct
    public void loadFormConfig() {
        try {
            ClassPathResource resource = new ClassPathResource("form-config.json");
            formConfig = objectMapper.readValue(resource.getInputStream(), FormConfig.class);
            logger.info("Successfully loaded form configuration with {} fields",
                formConfig.getFields().size());
        } catch (IOException e) {
            logger.error("Failed to load form-config.json", e);
            throw new RuntimeException("Failed to load form configuration", e);
        }
    }

    /**
     * Validates the form submission using JSON Logic rules from form-config.json
     *
     * @param dto The form submission data
     * @return List of validation error messages (empty if valid)
     */
    public List<String> validateFormSubmission(FormSubmissionDTO dto) {
        List<String> errors = new ArrayList<>();

        // Convert DTO to Map for JSON Logic evaluation
        Map<String, Object> model = convertDtoToMap(dto);

        // Iterate through all fields and check conditional requirements
        for (FieldConfig field : formConfig.getFields()) {
            if (field.getExpressions() != null &&
                field.getExpressions().containsKey("props.required")) {

                // Check if field is conditionally required
                boolean isRequired = evaluateJsonLogic(
                    field.getExpressions().get("props.required").getJsonLogic(),
                    model
                );

                if (isRequired) {
                    // Check if the field value is present
                    Object fieldValue = model.get(field.getKey());
                    if (fieldValue == null ||
                        (fieldValue instanceof String && ((String) fieldValue).isEmpty())) {

                        // Get custom error message from validation config
                        String errorMessage = getErrorMessage(field, "required");
                        errors.add(errorMessage);
                    }
                }
            }
        }

        return errors;
    }

    /**
     * Evaluate a JSON Logic expression
     *
     * @param jsonLogicRule The JSON Logic rule as a Map
     * @param data The data model to evaluate against
     * @return true if the expression evaluates to true, false otherwise
     */
    private boolean evaluateJsonLogic(Map<String, Object> jsonLogicRule, Map<String, Object> data) {
        try {
            // Wrap the data in a "model" object to match the JSON Logic expressions
            // The form-config.json uses "model.country", "model.hasLicense", etc.
            Map<String, Object> evaluationModel = new HashMap<>();
            evaluationModel.put("model", data);

            // Convert JSON Logic rule to JSON string
            String ruleJson = objectMapper.writeValueAsString(jsonLogicRule);

            // Apply the rule
            Object result = jsonLogic.apply(ruleJson, evaluationModel);

            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            logger.error("Error evaluating JSON Logic: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get error message for a field validation
     *
     * @param field The field configuration
     * @param validationType The type of validation (e.g., "required", "pattern")
     * @return The error message
     */
    private String getErrorMessage(FieldConfig field, String validationType) {
        if (field.getValidation() != null &&
            field.getValidation().containsKey("messages")) {

            @SuppressWarnings("unchecked")
            Map<String, String> messages = (Map<String, String>) field.getValidation().get("messages");

            if (messages.containsKey(validationType)) {
                return messages.get(validationType);
            }
        }

        // Default error message
        String label = field.getProps() != null && field.getProps().containsKey("label")
            ? (String) field.getProps().get("label")
            : field.getKey();

        return label + " is required";
    }

    /**
     * Convert DTO to Map for JSON Logic evaluation
     */
    private Map<String, Object> convertDtoToMap(FormSubmissionDTO dto) {
        return objectMapper.convertValue(dto, Map.class);
    }

    /**
     * Get the loaded form configuration (for testing/debugging)
     */
    public FormConfig getFormConfig() {
        return formConfig;
    }
}

