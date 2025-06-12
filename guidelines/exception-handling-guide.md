# Exception Handling - Hướng Dẫn Chi Tiết

Tài liệu này mô tả cách sử dụng exception handling trong library để đảm bảo error responses nhất quán.

## Tổng Quan

Library cung cấp `GlobalExceptionHandler` để tự động xử lý exceptions và convert thành `BaseResponse` format. Developers chỉ cần throw appropriate exceptions trong service layer.

## GlobalExceptionHandler

### Các Exception Types Được Xử Lý

1. **ResponseStatusException** - HTTP status exceptions với custom messages
2. **MethodArgumentNotValidException** - Bean validation failures  
3. **Exception** - Generic fallback cho unhandled exceptions

### Automatic Response Formatting

Tất cả exceptions được tự động convert thành `BaseResponse` format:

```json
{
  "code": 404,
  "message": "not_found",
  "errors": {
    "detail": "User not found"
  }
}
```

## Exception Patterns Trong Service Layer

### 1. Not Found Scenarios

```java
@Service
public class UserService {

    public UserDTO getById(Long id) {
        return userRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> 
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        CommonMessage.NOT_FOUND
                    ));
    }

    public UserDTO getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toDTO)
                .orElseThrow(() -> 
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "User with email " + email + " not found"
                    ));
    }
}
```

### 2. Conflict Scenarios (Resource Already Exists)

```java
@Service
public class UserService {

    public UserDTO create(CreateUserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                CommonMessage.EXISTING
            );
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Username already taken"
            );
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        // ... set other fields
        
        User saved = userRepository.save(user);
        return toDTO(saved);
    }
}
```

### 3. Business Logic Validation

```java
@Service
public class OrderService {

    public OrderDTO createOrder(CreateOrderRequest request) {
        // Validate product availability
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> 
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Product not found"
                    ));

        // Check stock availability
        if (product.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Insufficient stock available"
            );
        }

        // Check user credit limit
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> 
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "User not found"
                    ));

        BigDecimal totalAmount = product.getPrice().multiply(
            BigDecimal.valueOf(request.getQuantity())
        );

        if (user.getCreditLimit().compareTo(totalAmount) < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                CommonMessage.LIMIT
            );
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(totalAmount);
        
        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }
}
```

### 4. Permission/Authorization Errors

```java
@Service
public class DocumentService {

    public DocumentDTO getById(Long id, Long userId) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> 
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        CommonMessage.NOT_FOUND
                    ));

        // Check if user owns the document
        if (!document.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                CommonMessage.FORBIDDEN
            );
        }

        return toDTO(document);
    }

    public DocumentDTO update(Long id, UpdateDocumentRequest request, Long userId) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> 
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        CommonMessage.NOT_FOUND
                    ));

        // Check ownership
        if (!document.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "You can only edit your own documents"
            );
        }

        // Check if document is locked
        if (document.isLocked()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                CommonMessage.LOCKED
            );
        }

        // Update document
        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        
        Document saved = documentRepository.save(document);
        return toDTO(saved);
    }
}
```

### 5. Rate Limiting và Resource Limits

```java
@Service
public class ApiService {

    public void processRequest(String userId) {
        // Check rate limit
        int requestCount = rateLimitService.getRequestCount(userId);
        if (requestCount > MAX_REQUESTS_PER_HOUR) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS, 
                CommonMessage.LIMIT
            );
        }

        // Check resource quota
        long resourceUsage = resourceService.getUsage(userId);
        if (resourceUsage > MAX_RESOURCE_QUOTA) {
            throw new ResponseStatusException(
                HttpStatus.PAYMENT_REQUIRED, 
                "Resource quota exceeded"
            );
        }

        // Process request
        rateLimitService.incrementRequestCount(userId);
        processBusinessLogic();
    }
}
```

## Validation Errors

### Bean Validation Với @Valid

```java
// Request DTO với validation annotations
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Min(value = 18, message = "Age must be at least 18")
    private Integer age;

    // getters, setters
}

// Controller với @Valid
@PostMapping
public ResponseEntity<BaseResponse<UserDTO>> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    
    UserDTO user = userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.of(HttpStatus.CREATED.value(), CommonMessage.SUCCESS, user));
}
```

### Automatic Validation Error Response

Khi validation fails, `GlobalExceptionHandler` tự động tạo response:

```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email format is invalid",
    "username": "Username must be between 3 and 20 characters",
    "password": "Password must be at least 8 characters"
  }
}
```

## Custom Validation

### Custom Validator Annotation

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) return true; // Let @NotBlank handle null
        return !userRepository.existsByEmail(email);
    }
}

// Sử dụng trong Request DTO
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @UniqueEmail
    private String email;
    
    // other fields
}
```

### Manual Validation Trong Service

```java
@Service
public class UserService {

    public UserDTO create(CreateUserRequest request) {
        // Manual validation với custom logic
        validateCreateUserRequest(request);
        
        User user = new User();
        // ... create user
        
        return toDTO(userRepository.save(user));
    }

    private void validateCreateUserRequest(CreateUserRequest request) {
        List<String> errors = new ArrayList<>();

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            errors.add("Email already exists");
        }

        // Check username format
        if (!request.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            errors.add("Username can only contain letters, numbers, and underscores");
        }

        // Check password strength
        if (!isPasswordStrong(request.getPassword())) {
            errors.add("Password must contain at least one uppercase, one lowercase, and one number");
        }

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                String.join(", ", errors)
            );
        }
    }

    private boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
    }
}
```

## HTTP Status Code Guidelines

### 4xx Client Errors

- **400 BAD_REQUEST**: Invalid input, validation errors, business logic violations
- **401 UNAUTHORIZED**: Authentication required hoặc invalid credentials
- **403 FORBIDDEN**: Authenticated but insufficient permissions
- **404 NOT_FOUND**: Resource không tồn tại
- **409 CONFLICT**: Resource already exists hoặc conflicting state
- **422 UNPROCESSABLE_ENTITY**: Valid format but business rules violated
- **429 TOO_MANY_REQUESTS**: Rate limiting exceeded

### 5xx Server Errors

- **500 INTERNAL_SERVER_ERROR**: Unexpected server errors

## CommonMessage Usage

### Standard Messages

```java
// Success
CommonMessage.SUCCESS          // "success"

// Client errors
CommonMessage.INVALID          // "invalid"
CommonMessage.REQUIRED         // "required"
CommonMessage.NOT_FOUND        // "not_found"
CommonMessage.EXISTING         // "existing"
CommonMessage.UNAUTHORIZED     // "unauthorized"
CommonMessage.FORBIDDEN        // "forbidden"
CommonMessage.CONFLICT         // "conflict"
CommonMessage.LIMIT            // "limit"
CommonMessage.LOCKED           // "locked"

// Server errors
CommonMessage.INTERNAL_SERVER  // "internal_server"
CommonMessage.FAIL             // "fail"
```

### Custom Messages

```java
// Sử dụng custom message cho specific scenarios
throw new ResponseStatusException(
    HttpStatus.BAD_REQUEST, 
    "Order cannot be cancelled after 24 hours"
);

// Hoặc combine với CommonMessage
throw new ResponseStatusException(
    HttpStatus.CONFLICT, 
    CommonMessage.EXISTING + ": User with this email already exists"
);
```

## Best Practices

### 1. Consistent Error Messages
- Sử dụng `CommonMessage` constants khi có thể
- Provide clear, actionable error messages
- Không expose sensitive information

### 2. Appropriate HTTP Status Codes
- Use correct status codes theo HTTP standards
- 4xx cho client errors, 5xx cho server errors
- Be specific: 404 vs 403 vs 400

### 3. Validation Strategy
- Use Bean Validation cho basic validation
- Implement business validation trong service layer
- Fail fast - validate early trong request processing

### 4. Error Context
- Include relevant context trong error messages
- Avoid generic "Something went wrong" messages
- Help users understand how to fix the error

### 5. Security Considerations
- Không leak sensitive data trong error messages
- Use generic messages cho authentication failures
- Log detailed errors server-side cho debugging

## Error Response Examples

### Validation Error
```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email format is invalid",
    "age": "Age must be at least 18"
  }
}
```

### Business Logic Error
```json
{
  "code": 400,
  "message": "Insufficient stock available",
  "errors": {
    "available": 5,
    "requested": 10
  }
}
```

### Authorization Error
```json
{
  "code": 403,
  "message": "forbidden"
}
```

### Not Found Error
```json
{
  "code": 404,
  "message": "not_found"
}
```