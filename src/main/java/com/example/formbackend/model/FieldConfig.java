package com.example.formbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Model for individual field configuration
 */
@Data
public class FieldConfig {
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("props")
    private Map<String, Object> props;
    
    @JsonProperty("validation")
    private Map<String, Object> validation;
    
    @JsonProperty("expressions")
    private Map<String, Expression> expressions;
}

