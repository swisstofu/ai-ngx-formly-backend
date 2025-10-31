package com.example.formbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Model for the form configuration JSON
 */
@Data
public class FormConfig {
    @JsonProperty("fields")
    private List<FieldConfig> fields;
}

