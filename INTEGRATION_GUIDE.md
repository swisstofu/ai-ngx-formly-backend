# Backend Integration Guide

## Overview

This Spring Boot backend provides a REST API for validating and processing form submissions from the Angular frontend. It implements both standard Bean Validation and JSON Logic-based conditional validation to match the frontend's dynamic form behavior.

## Key Features

### 1. Bean Validation (JSR-303/JSR-380)

Standard Java validation annotations on the `FormSubmissionDTO`:

```java
@NotBlank(message = "First name is required")
@Size(min = 2, message = "First name must be at least 2 characters")
@ValidName(message = "Only letters, spaces, hyphens, and apostrophes are allowed")
private String firstName;
```

### 2. Custom Validators

**ValidName Validator:**
- Pattern: `^[a-zA-ZÀ-ÿ\s'-]+$`
- Allows: letters, spaces, hyphens, apostrophes
- Supports: international characters (À-ÿ)

**ValidLicenseNumber Validator:**
- Pattern: `^[A-Z0-9]{6,12}$`
- Requires: 6-12 uppercase letters or numbers

### 3. JSON Logic Validation

The `FormValidationService` applies the same JSON Logic rules as the frontend:

**Age required for US residents:**
```json
{"===": [{"var": "country"}, "us"]}
```

**License number required if hasLicense:**
```json
{"==": [{"var": "hasLicense"}, true]}
```

**Company name required for employed/self-employed:**
```json
{"in": [{"var": "employmentStatus"}, ["employed", "self-employed"]]}
```

**Annual income required for certain employment + age >= 18:**
```json
{
  "and": [
    {"in": [{"var": "employmentStatus"}, ["employed", "self-employed", "retired"]]},
    {">=": [{"var": "age"}, 18]}
  ]
}
```

## API Response Format

### Success Response

```json
{
  "success": true,
  "message": "Form submitted successfully!",
  "data": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    ...
  },
  "submittedAt": "2025-10-30T12:34:56.789Z"
}
```

### Bean Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "fieldErrors": {
    "firstName": "First name must be at least 2 characters",
    "email": "Please enter a valid email address"
  }
}
```

### JSON Logic Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    "Age is required for US residents",
    "License number is required when you have a driver's license"
  ]
}
```

## CORS Configuration

The backend is configured to accept requests from:
- `http://localhost:4200` (Angular default)
- `http://localhost:3000` (Alternative frontend port)

To add more origins, edit `WebConfig.java`:

```java
.allowedOrigins(
    "http://localhost:4200",
    "http://localhost:3000",
    "https://your-production-domain.com"
)
```

## Testing the API

### Using curl

```bash
# Valid submission
curl -X POST http://localhost:8080/api/forms/submit \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "country": "us",
    "age": 25
  }'
```

### Using the HTTP file

Open `example-requests.http` in VS Code or IntelliJ IDEA with REST Client extension and click "Send Request" on any example.

### Using the Frontend

1. Start the backend: `mvn spring-boot:run`
2. Start the frontend: `cd frontend && npm start`
3. Open `http://localhost:4200`
4. Fill out and submit the form

## Extending the Backend

### Adding New Fields

1. **Add field to DTO:**
```java
@NotBlank(message = "Phone is required")
@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
private String phone;
```

2. **Add JSON Logic validation (if conditional):**
```java
private boolean isPhoneRequired(Map<String, Object> model) {
    String rule = "{\"===\": [{\"var\": \"country\"}, \"us\"]}";
    Object result = jsonLogic.apply(rule, model);
    return Boolean.TRUE.equals(result);
}
```

3. **Add validation in service:**
```java
if (isPhoneRequired(model) && 
    (dto.getPhone() == null || dto.getPhone().isEmpty())) {
    errors.add("Phone is required for US residents");
}
```

### Adding Custom Validators

1. **Create annotation:**
```java
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

2. **Create validator:**
```java
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}
```

3. **Apply to DTO field:**
```java
@ValidPhone(message = "Invalid phone number format")
private String phone;
```

## Logging

The application logs validation errors and successful submissions:

```
2025-10-30 12:34:56 - Received form submission: FormSubmissionDTO(firstName=John, ...)
2025-10-30 12:34:56 - Form validation successful. Processing submission...
```

To change log levels, edit `application.properties`:

```properties
logging.level.com.example.formbackend=DEBUG
```

## Error Handling

The `FormController` includes exception handlers for:

1. **MethodArgumentNotValidException**: Bean Validation errors
2. **Generic Exception**: Unexpected errors

All errors are returned in a consistent JSON format with appropriate HTTP status codes.

## Security Considerations

**Current Implementation:**
- CORS enabled for local development
- No authentication/authorization
- No rate limiting
- No input sanitization beyond validation

**Production Recommendations:**
- Add Spring Security for authentication
- Implement rate limiting (e.g., Bucket4j)
- Add CSRF protection
- Sanitize inputs to prevent XSS
- Use HTTPS only
- Implement request logging and monitoring
- Add API versioning

## Performance

**Current Configuration:**
- Embedded Tomcat server
- Default thread pool
- No caching
- No database

**Production Recommendations:**
- Add database for persistence
- Implement caching (Redis, Caffeine)
- Configure connection pooling
- Add monitoring (Actuator, Prometheus)
- Optimize JSON serialization
- Consider async processing for heavy operations

## Dependencies

Key dependencies in `pom.xml`:

- **spring-boot-starter-web**: REST API support
- **spring-boot-starter-validation**: Bean Validation
- **json-logic-java**: JSON Logic evaluation
- **lombok**: Reduce boilerplate code
- **jackson-databind**: JSON processing

## Troubleshooting

**Issue: CORS errors in browser**
- Solution: Check `WebConfig.java` allowed origins
- Verify backend is running on port 8080

**Issue: Validation not working**
- Solution: Check `@Valid` annotation on controller method
- Verify validation annotations on DTO fields

**Issue: JSON Logic validation not applied**
- Solution: Check `FormValidationService` is autowired
- Verify JSON Logic rules match frontend

**Issue: Port 8080 already in use**
- Solution: Change port in `application.properties`:
  ```properties
  server.port=8081
  ```

## Next Steps

1. **Add Database**: Persist form submissions
2. **Add Authentication**: Secure the API
3. **Add Email**: Send confirmation emails
4. **Add File Upload**: Support document uploads
5. **Add Audit Log**: Track all submissions
6. **Add Metrics**: Monitor API performance
7. **Add Documentation**: Swagger/OpenAPI

