# API Response Patterns - Hướng Dẫn Chi Tiết

Tài liệu này mô tả cách sử dụng `BaseResponse<T>`, `Pagination`, và `CommonMessage` để standardize API responses.

## Cấu Trúc BaseResponse

### Định Nghĩa
```java
public record BaseResponse<T>(
    Integer code,           // HTTP status code
    String message,         // Message mô tả response
    T data,                // Dữ liệu response chính
    Pagination pagination, // Metadata phân trang (optional)
    Map<String, Object> errors // Chi tiết lỗi (optional)
)
```

### Các Field

- **code**: HTTP status code (200, 400, 404, etc.)
- **message**: Human-readable message, sử dụng `CommonMessage` constants
- **data**: Generic type chứa actual response data
- **pagination**: Metadata cho paginated responses
- **errors**: Map chứa detailed error information

## Factory Methods

### 1. Response Chỉ Với Data
```java
User user = userService.findById(1L);
BaseResponse<User> response = BaseResponse.of(user);

// JSON output:
{
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### 2. Response Với Data và Pagination
```java
Page<User> userPage = userService.findAll(pageRequest);
Pagination pagination = new Pagination(
    userPage.getNumber(),
    userPage.getSize(), 
    userPage.getTotalElements()
);

BaseResponse<List<User>> response = BaseResponse.of(userPage.getContent(), pagination);

// JSON output:
{
  "data": [
    {"id": 1, "name": "John"},
    {"id": 2, "name": "Jane"}
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "total": 25
  }
}
```

### 3. Response Với Code và Message
```java
BaseResponse<?> response = BaseResponse.of(200, CommonMessage.SUCCESS);

// JSON output:
{
  "code": 200,
  "message": "success"
}
```

### 4. Response Với Code, Message và Data
```java
User user = userService.create(request);
BaseResponse<User> response = BaseResponse.of(201, CommonMessage.SUCCESS, user);

// JSON output:
{
  "code": 201,
  "message": "success",
  "data": {
    "id": 1,
    "name": "John Doe"
  }
}
```

### 5. Error Response Với Details
```java
Map<String, Object> errors = new HashMap<>();
errors.put("email", "Email already exists");
errors.put("phone", "Invalid phone format");

BaseResponse<?> response = BaseResponse.of(400, CommonMessage.INVALID, errors);

// JSON output:
{
  "code": 400,
  "message": "invalid",
  "errors": {
    "email": "Email already exists",
    "phone": "Invalid phone format"
  }
}
```

### 6. Response Từ Exception
```java
try {
    // Some operation
} catch (ResponseStatusException ex) {
    BaseResponse<?> response = BaseResponse.of(ex);
    // Automatically extracts status code and message
}
```

## Pagination Object

```java
public record Pagination(
    int page,      // Current page (0-based)
    int size,      // Page size
    long total     // Total elements
)
```

## Controller Patterns

### GET Single Resource
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<User>> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(BaseResponse.of(user));
    }
}
```

### GET Collection With Pagination
```java
@GetMapping
public ResponseEntity<BaseResponse<List<User>>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<User> users = userService.findAll(pageable);
    
    Pagination pagination = new Pagination(
        users.getNumber(),
        users.getSize(),
        users.getTotalElements()
    );
    
    return ResponseEntity.ok(BaseResponse.of(users.getContent(), pagination));
}
```

### POST Create Resource
```java
@PostMapping
public ResponseEntity<BaseResponse<User>> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    
    User user = userService.create(request);
    
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(BaseResponse.of(HttpStatus.CREATED.value(), CommonMessage.SUCCESS, user));
}
```

### PUT Update Resource
```java
@PutMapping("/{id}")
public ResponseEntity<BaseResponse<User>> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest request) {
    
    User user = userService.update(id, request);
    
    return ResponseEntity.ok(
        BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS, user)
    );
}
```

### DELETE Resource
```java
@DeleteMapping("/{id}")
public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteById(id);
    
    return ResponseEntity.ok(
        BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS)
    );
}
```

## HTTP Status Codes

### Success Responses
- **200 OK**: GET, PUT, DELETE thành công
- **201 CREATED**: POST tạo resource thành công
- **204 NO_CONTENT**: DELETE thành công (không trả data)

### Client Error Responses
- **400 BAD_REQUEST**: Invalid input, validation errors
- **401 UNAUTHORIZED**: Authentication required
- **403 FORBIDDEN**: Permission denied
- **404 NOT_FOUND**: Resource not found
- **409 CONFLICT**: Resource already exists

### Server Error Responses
- **500 INTERNAL_SERVER_ERROR**: Unexpected server error

## CommonMessage Constants

```java
public static final String SUCCESS = "success";
public static final String FAIL = "fail";
public static final String INVALID = "invalid";
public static final String EXISTING = "existing";
public static final String REQUIRED = "required";
public static final String NOT_FOUND = "not_found";
public static final String UNAUTHORIZED = "unauthorized";
public static final String FORBIDDEN = "forbidden";
public static final String CONFLICT = "conflict";
public static final String INTERNAL_SERVER = "internal_server";
public static final String LIMIT = "limit";
public static final String LOCKED = "locked";
```

## Service Layer Examples

### Repository Pattern
```java
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> 
                    new ResponseStatusException(HttpStatus.NOT_FOUND, CommonMessage.NOT_FOUND));
    }

    public User create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, CommonMessage.EXISTING);
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        
        return userRepository.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CommonMessage.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }
}
```

## Error Response Examples

### Validation Error (Automatic)
```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email format is invalid",
    "name": "Name is required"
  }
}
```

### Business Logic Error
```json
{
  "code": 409,
  "message": "existing"
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

## Best Practices

### 1. Consistency
- Luôn sử dụng `BaseResponse<T>` cho mọi API endpoint
- Sử dụng đúng HTTP status codes
- Sử dụng `CommonMessage` constants

### 2. Error Handling
- Throw `ResponseStatusException` trong service layer
- Sử dụng `GlobalExceptionHandler` (được cung cấp bởi library)
- Không expose sensitive information

### 3. Pagination
- Luôn include pagination cho list responses
- Sử dụng reasonable default page sizes (10-20)
- Support sorting parameters nếu cần

### 4. Data Transfer
- Sử dụng DTOs thay vì expose entities trực tiếp
- Validate input tại controller layer với `@Valid`
- Keep response payloads focused và relevant

### 5. Performance
- Avoid N+1 queries trong data loading
- Use appropriate fetch strategies
- Consider caching cho frequently accessed data

## Example Entity với BaseModel

```java
@Entity
@Table(name = "users")
public class User extends BaseModel {
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String phone;
    
    // Constructor, getters, setters
    // ID, createdAt, updatedAt inherited từ BaseModel
}
```

## Validation Examples

```java
public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String phone;
    
    // getters, setters
}
```

Validation errors sẽ tự động được handle bởi `GlobalExceptionHandler` và convert thành `BaseResponse` format với errors field populated.