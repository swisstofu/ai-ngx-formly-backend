# Form Configuration-Based Validation

## Overview

The backend now uses a **copy of the frontend's `form-config.json`** file to perform JSON Logic validation. This ensures that validation rules are consistent between frontend and backend, and they're defined in a single source of truth.

## Architecture

### Single Source of Truth

```
frontend/src/app/dynamic-form/form-config.json  ← Original configuration
                    ↓ (copied to)
backend/src/main/resources/form-config.json     ← Backend copy
                    ↓ (loaded by)
FormValidationService.java                      ← Applies JSON Logic rules
```

### Benefits

1. **Consistency**: Same validation rules on frontend and backend
2. **Maintainability**: Update rules in one place, copy to backend
3. **Flexibility**: Add new fields and rules without code changes
4. **Transparency**: Validation logic is declarative, not imperative

## How It Works

### 1. Form Configuration Loading

On application startup, `FormValidationService` loads the `form-config.json` file:

```java
@PostConstruct
public void loadFormConfig() {
    ClassPathResource resource = new ClassPathResource("form-config.json");
    formConfig = objectMapper.readValue(resource.getInputStream(), FormConfig.class);
}
```

### 2. Validation Process

When a form is submitted:

1. **Bean Validation** runs first (annotations on DTO)
2. **JSON Logic Validation** runs second (from form-config.json)

```java
public List<String> validateFormSubmission(FormSubmissionDTO dto) {
    List<String> errors = new ArrayList<>();
    Map<String, Object> model = convertDtoToMap(dto);
    
    // Iterate through all fields
    for (FieldConfig field : formConfig.getFields()) {
        if (field.getExpressions() != null && 
            field.getExpressions().containsKey("props.required")) {
            
            // Evaluate JSON Logic expression
            boolean isRequired = evaluateJsonLogic(
                field.getExpressions().get("props.required").getJsonLogic(), 
                model
            );
            
            if (isRequired) {
                // Check if field value is present
                Object fieldValue = model.get(field.getKey());
                if (fieldValue == null || isEmpty(fieldValue)) {
                    errors.add(getErrorMessage(field, "required"));
                }
            }
        }
    }
    
    return errors;
}
```

### 3. JSON Logic Evaluation

The service evaluates JSON Logic expressions from the configuration:

```java
private boolean evaluateJsonLogic(Map<String, Object> jsonLogicRule, Map<String, Object> model) {
    String ruleJson = objectMapper.writeValueAsString(jsonLogicRule);
    Object result = jsonLogic.apply(ruleJson, model);
    return Boolean.TRUE.equals(result);
}
```

## Form Configuration Structure

### Field Configuration Example

```json
{
  "key": "age",
  "type": "input",
  "props": {
    "label": "Age",
    "type": "number",
    "min": 0,
    "max": 120
  },
  "validation": {
    "messages": {
      "required": "Age is required for US residents",
      "min": "Age must be at least 0",
      "max": "Age must be less than 120"
    }
  },
  "expressions": {
    "props.required": {
      "jsonLogic": {
        "===": [{ "var": "model.country" }, "us"]
      }
    }
  }
}
```

### Key Components

1. **`key`**: Field identifier (matches DTO property)
2. **`props`**: Field properties (label, type, min, max, etc.)
3. **`validation.messages`**: Custom error messages
4. **`expressions.props.required`**: JSON Logic rule for conditional requirement

## Validation Rules in Form Config

### Age Required for US Residents

```json
"expressions": {
  "props.required": {
    "jsonLogic": {
      "===": [{ "var": "model.country" }, "us"]
    }
  }
}
```

**Meaning**: Age is required when country equals "us"

### License Number Required if Has License

```json
"expressions": {
  "props.required": {
    "jsonLogic": {
      "==": [{ "var": "model.hasLicense" }, true]
    }
  }
}
```

**Meaning**: License number is required when hasLicense is true

### Company Name Required for Employed/Self-Employed

```json
"expressions": {
  "props.required": {
    "jsonLogic": {
      "in": [
        { "var": "model.employmentStatus" },
        ["employed", "self-employed"]
      ]
    }
  }
}
```

**Meaning**: Company name is required when employment status is "employed" or "self-employed"

### Annual Income Required (Complex Rule)

```json
"expressions": {
  "props.required": {
    "jsonLogic": {
      "and": [
        {
          "in": [
            { "var": "model.employmentStatus" },
            ["employed", "self-employed", "retired"]
          ]
        },
        { ">=": [{ "var": "model.age" }, 18] }
      ]
    }
  }
}
```

**Meaning**: Annual income is required when:
- Employment status is "employed", "self-employed", or "retired" AND
- Age is 18 or older

## Model Classes

### FormConfig.java

```java
@Data
public class FormConfig {
    @JsonProperty("fields")
    private List<FieldConfig> fields;
}
```

### FieldConfig.java

```java
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
```

### Expression.java

```java
@Data
public class Expression {
    @JsonProperty("jsonLogic")
    private Map<String, Object> jsonLogic;
}
```

## API Endpoints

### Get Form Configuration

```http
GET /api/forms/config
```

**Response:**
```json
{
  "fields": [
    {
      "key": "firstName",
      "type": "input",
      "props": { ... },
      "validation": { ... }
    },
    ...
  ]
}
```

**Use Case**: Frontend can fetch the configuration from the backend instead of bundling it

### Submit Form

```http
POST /api/forms/submit
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "country": "us",
  "age": 25
}
```

**Success Response:**
```json
{
  "success": true,
  "message": "Form submitted successfully!",
  "data": { ... },
  "submittedAt": "2025-10-30T12:34:56.789Z"
}
```

**Validation Error Response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    "Age is required for US residents"
  ]
}
```

## Updating Validation Rules

### Step 1: Update Frontend Configuration

Edit `frontend/src/app/dynamic-form/form-config.json`:

```json
{
  "key": "newField",
  "type": "input",
  "props": {
    "label": "New Field",
    "required": true
  },
  "expressions": {
    "props.required": {
      "jsonLogic": {
        "===": [{ "var": "model.someCondition" }, true]
      }
    }
  }
}
```

### Step 2: Copy to Backend

```bash
cp frontend/src/app/dynamic-form/form-config.json backend/src/main/resources/form-config.json
```

### Step 3: Update DTO (if new field)

Add the new field to `FormSubmissionDTO.java`:

```java
@NotBlank(message = "New field is required")
private String newField;
```

### Step 4: Restart Backend

The new validation rules will be loaded automatically on startup.

## Testing

### Unit Tests

The existing tests in `FormControllerTest.java` still work because they mock the `FormValidationService`.

### Manual Testing

Use the `example-requests.http` file:

```http
### Get Form Configuration
GET http://localhost:8080/api/forms/config

### Submit Valid Form
POST http://localhost:8080/api/forms/submit
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "country": "us",
  "age": 25
}

### Submit Invalid Form (Missing Age for US)
POST http://localhost:8080/api/forms/submit
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "country": "us"
}
```

## Troubleshooting

### Form Config Not Loading

**Error**: `Failed to load form configuration`

**Solution**: 
- Ensure `form-config.json` is in `backend/src/main/resources/`
- Check JSON syntax is valid
- Rebuild the project: `mvn clean install`

### Validation Not Working

**Error**: Fields not being validated

**Solution**:
- Check that field keys in form-config.json match DTO property names
- Verify JSON Logic expressions are correct
- Check backend logs for evaluation errors

### Custom Error Messages Not Showing

**Error**: Generic error messages instead of custom ones

**Solution**:
- Ensure `validation.messages` is defined in form-config.json
- Check the validation type matches (e.g., "required", "pattern")

## Best Practices

1. **Keep Frontend and Backend in Sync**: Always copy form-config.json to backend after changes
2. **Version Control**: Commit both copies of form-config.json
3. **Validation Messages**: Use clear, user-friendly error messages
4. **Test Both Layers**: Test validation on both frontend and backend
5. **Document Rules**: Add comments in form-config.json for complex rules

## Future Enhancements

1. **Shared Configuration**: Use a build script to automatically copy form-config.json
2. **Dynamic Loading**: Allow hot-reloading of configuration without restart
3. **Validation Library**: Extract common validation rules into a shared library
4. **Schema Validation**: Validate form-config.json against a JSON schema
5. **Admin UI**: Create an admin interface to edit validation rules

## Conclusion

Using form-config.json for backend validation provides:
- ✅ Single source of truth for validation rules
- ✅ Consistency between frontend and backend
- ✅ Declarative, maintainable validation logic
- ✅ Easy to extend with new fields and rules
- ✅ Custom error messages from configuration

This approach makes the application more maintainable and reduces the risk of validation inconsistencies between frontend and backend.

