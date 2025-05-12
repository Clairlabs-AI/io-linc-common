# Service Common Library

A common utility library for Spring Boot microservices that provides cross-cutting concerns like security, logging, and request handling.

## Features

- JWT authentication and validation
- Request header validation
- ThreadLocal context for request scoping
- Comprehensive audit logging
- Performance monitoring with aspect-oriented programming
- Structured logging with JSON format
- Global exception handling
- Correlation ID tracking across services

## Requirements

- Java 21
- Spring Boot 3.x

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.medgenome</groupId>
    <artifactId>service-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Basic Setup

The library auto-configures itself when added as a dependency. To use it in your Spring Boot application:

```java
@SpringBootApplication
public class AnyApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnyApplication.class, args);
    }
}
```

### Configuration

Configure the library using your `application.yml` or `application.properties`:

```yml
app:
  security:
    jwt:
      secret-key: your-secret-key
      expiration-ms: 86400000
  
  logging:
    performance:
      enabled: true
      default-warn-threshold-ms: 1000
```

### Using JWT Authentication

JWT is automatically configured. To secure endpoints, just use Spring Security annotations:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/login")
    public ResponseEntity<UserDto> getCurrentUser() {
        // Access user info from RequestContext
        String userId = RequestContext.current().getUserId();
        // ...
    }
}
```

### Audit Logging

Use the `@AuditLog` annotation to log important operations:

```java
@Service
public class UserService {
    
    @AuditLog(action = "CREATE_USER", includeArgs = true)
    public User createUser(UserRequest request) {
        // Create user logic
        return user;
    }
}
```

### Performance Monitoring

Use the `@LogPerformance` annotation to monitor method execution time:

```java
@Service
public class PatientService {
    
    @LogPerformance(warnThresholdMs = 500)
    public List<PatientDetails> searchPatients(SearchCriteria criteria) {
        // Search logic
        return patients;
    }
}
```

## Advanced Usage

### Customizing Header Validation

Configure required headers and validation rules:

```yml
app:
  security:
    header-validation:
      required-headers:
        - X-MessageId
        - X-CorrelationId
      validation-rules:
        X-MessageId:
          pattern: "[A-Za-z0-9]{16}"
          required: true
```

### Accessing Request Context

Access the current request context anywhere:

```java
String userId = RequestContext.current().getUserId();
String correlationId = RequestContext.current().getCorrelationId();

// Add custom attributes
RequestContext.current().setAttribute("customKey", "customValue");
```

