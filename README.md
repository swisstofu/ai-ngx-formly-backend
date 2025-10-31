# Dynamic Form Backend API

A Spring Boot REST API for handling dynamic form submissions with JSON Logic-based validation.

## Features

- **REST API** for form submission
- **Bean Validation (JSR-303/JSR-380)** for input validation
- **JSON Logic** for conditional validation rules
- **Custom validators** for name, email, and license number
- **CORS configuration** for frontend integration
- **Comprehensive error handling** with structured error responses

## Technology Stack

- **Java 21 (OpenJDK)**
- **Spring Boot 3.5.7**
- **Logback 1.5.20** - Logging (fixes CVE-2023-6378, CVE-2024-12798)
- **Spring Web** - REST API
- **Spring Validation** - Bean Validation
- **JSON Logic Java** - Conditional validation
- **Lombok** - Reduce boilerplate code
- **Maven** - Build tool

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/formbackend/
│   │   │   ├── FormBackendApplication.java      # Main application class
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java               # CORS configuration
│   │   │   ├── controller/
│   │   │   │   └── FormController.java          # REST endpoints
│   │   │   ├── dto/
│   │   │   │   └── FormSubmissionDTO.java       # Form data transfer object
│   │   │   ├── service/
│   │   │   │   └── FormValidationService.java   # JSON Logic validation
│   │   │   └── validation/
│   │   │       ├── ValidName.java               # Name validation annotation
│   │   │       ├── NameValidator.java           # Name validator implementation
│   │   │       ├── ValidLicenseNumber.java      # License validation annotation
│   │   │       └── LicenseNumberValidator.java  # License validator implementation
│   │   └── resources/
│   │       └── application.properties           # Application configuration
│   └── test/
│       └── java/                                # Test files
├── pom.xml                                      # Maven configuration
└── README.md                                    # This file
```

## API Endpoints

### 1. Submit Form

**Endpoint:** `POST /api/forms/submit`

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "country": "us",
  "state": "ca",
  "age": 25,
  "hasLicense": true,
  "licenseNumber": "ABC123456",
  "employmentStatus": "employed",
  "companyName": "Tech Corp",
  "annualIncome": 75000,
  "comments": "Additional information"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Form submitted successfully!",
  "data": { ... },
  "submittedAt": "2025-10-30T12:34:56.789Z"
}
```

**Validation Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Validation failed",
  "fieldErrors": {
    "firstName": "First name is required",
    "email": "Please enter a valid email address"
  }
}
```

### 2. Health Check

**Endpoint:** `GET /api/forms/health`

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "Form Backend API"
}
```

## Validation Rules

### Basic Validation (Bean Validation)

- **firstName**: Required, min 2 characters, letters/spaces/hyphens/apostrophes only
- **lastName**: Required, min 2 characters, letters/spaces/hyphens/apostrophes only
- **email**: Required, valid email format
- **country**: Required
- **age**: 0-120 (if provided)
- **licenseNumber**: 6-12 uppercase letters/numbers (if provided)
- **companyName**: Min 2 characters (if provided)
- **annualIncome**: Min 0 (if provided)

### Conditional Validation (JSON Logic)

- **age**: Required if country is "us"
- **licenseNumber**: Required if hasLicense is true
- **companyName**: Required if employmentStatus is "employed" or "self-employed"
- **annualIncome**: Required if employmentStatus is "employed", "self-employed", or "retired" AND age >= 18

## Getting Started

### Prerequisites

- Java 21 (OpenJDK) or higher
- Maven 3.6 or higher

### Installation

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080/api`

### Testing the API

You can test the API using curl:

```bash
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

Or use tools like Postman, Insomnia, or the Angular frontend.

## Configuration

### Application Properties

Edit `src/main/resources/application.properties` to configure:

- Server port (default: 8080)
- Context path (default: /api)
- Logging levels
- Jackson JSON serialization

### CORS Configuration

Edit `src/main/java/com/example/formbackend/config/WebConfig.java` to configure:

- Allowed origins (default: http://localhost:4200, http://localhost:3000)
- Allowed methods
- Allowed headers

## Development

### Adding New Validation Rules

1. Create a new annotation in `validation/` package
2. Implement the validator class
3. Apply the annotation to the DTO field

### Adding New Endpoints

1. Add methods to `FormController.java`
2. Use `@GetMapping`, `@PostMapping`, etc.
3. Add appropriate validation and error handling

## Integration with Frontend

The backend is designed to work with the Angular frontend in the `frontend/` directory.

1. Start the backend: `mvn spring-boot:run`
2. Start the frontend: `cd frontend && npm start`
3. Access the application at `http://localhost:4200`

The frontend will automatically send form submissions to `http://localhost:8080/api/forms/submit`

## License

This project is part of the ai-ngx-formly demonstration.

