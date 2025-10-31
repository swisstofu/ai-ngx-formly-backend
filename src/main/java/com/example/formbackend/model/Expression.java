package com.example.formbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Model for JSON Logic expressions
 */
@Data
public class Expression {
    @JsonProperty("jsonLogic")
    private Map<String, Object> jsonLogic;
}

