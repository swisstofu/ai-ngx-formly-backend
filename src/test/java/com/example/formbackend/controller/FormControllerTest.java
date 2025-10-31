package com.example.formbackend.controller;

import com.example.formbackend.dto.FormSubmissionDTO;
import com.example.formbackend.model.FieldConfig;
import com.example.formbackend.model.FormConfig;
import com.example.formbackend.service.FormValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FormController
 */
@WebMvcTest(FormController.class)
class FormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FormValidationService validationService;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/forms/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Form Backend API"));
    }

    @Test
    void testGetFormConfig() throws Exception {
        // Arrange - Create a mock form config
        FormConfig mockConfig = new FormConfig();
        FieldConfig field1 = new FieldConfig();
        field1.setKey("firstName");
        field1.setType("input");

        FieldConfig field2 = new FieldConfig();
        field2.setKey("lastName");
        field2.setType("input");

        mockConfig.setFields(Arrays.asList(field1, field2));

        when(validationService.getFormConfig()).thenReturn(mockConfig);

        // Act & Assert
        mockMvc.perform(get("/forms/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields[0].key").value("firstName"))
                .andExpect(jsonPath("$.fields[1].key").value("lastName"));
    }

    @Test
    void testSubmitFormSuccess() throws Exception {
        // Arrange
        FormSubmissionDTO dto = new FormSubmissionDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setCountry("us");
        dto.setAge(25);

        when(validationService.validateFormSubmission(any())).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(post("/forms/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Form submitted successfully!"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }

    @Test
    void testSubmitFormWithBeanValidationErrors() throws Exception {
        // Arrange - Missing required fields
        FormSubmissionDTO dto = new FormSubmissionDTO();
        dto.setFirstName("J"); // Too short
        dto.setEmail("invalid-email"); // Invalid format

        // Act & Assert
        mockMvc.perform(post("/forms/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void testSubmitFormWithJsonLogicValidationErrors() throws Exception {
        // Arrange
        FormSubmissionDTO dto = new FormSubmissionDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setCountry("us");
        // Missing age (required for US residents)

        when(validationService.validateFormSubmission(any()))
                .thenReturn(Arrays.asList("Age is required for US residents"));

        // Act & Assert
        mockMvc.perform(post("/forms/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("Age is required for US residents"));
    }

    @Test
    void testSubmitFormWithLicenseValidation() throws Exception {
        // Arrange
        FormSubmissionDTO dto = new FormSubmissionDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setCountry("ca");
        dto.setAge(25);
        dto.setHasLicense(true);
        dto.setLicenseNumber("ABC123"); // Valid format

        when(validationService.validateFormSubmission(any())).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(post("/forms/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

