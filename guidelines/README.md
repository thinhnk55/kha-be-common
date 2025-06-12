# KHA-BE-Common Library - Hướng Dẫn Sử Dụng

Thư viện cung cấp các utilities cơ bản cho Spring Boot applications bao gồm: API response standardization, JWT authentication, Casbin authorization, exception handling, và web utilities.

## Những Gì Library Cung Cấp

✅ **API Response Framework**: `BaseResponse<T>`, `Pagination`, `CommonMessage`  
✅ **JWT Authentication**: Token generation/validation với RS256  
✅ **Casbin Authorization**: `@CasbinAuthorize` annotation và policy loading  
✅ **Exception Handling**: `GlobalExceptionHandler` cho standardized errors  
✅ **Web Utilities**: `@IpAddress` và `@UserAgent` parameter injection  
✅ **Base Models**: JPA entities với timestamps  

❌ **Những Gì Library KHÔNG Cung Cấp**: User management, session storage, authentication endpoints, rate limiting, database configuration

## Thiết Lập Dự Án

### 1. Thêm Dependency

```kotlin
// build.gradle.kts
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.thinhnk55:kha-be-common:1.0.8")
}
```

### 2. Enable Configuration

```java
@Configuration
@ComponentScan(basePackages = {"com.defi.common", "com.yourapp"})
public class AppConfig {
}
```

### 3. Cấu Hình Cơ Bản

```yaml
# application.yml

# CORS - CHỈ support origins
app:
  cors:
    origins:
      - "http://localhost:3000"
      - "https://yourdomain.com"

# Casbin Authorization
app:
  casbin:
    policy-source: "database:SELECT role_name, resource_code, action_name FROM permissions"
    resources:
      - "user"
      - "document"
    polling:
      enabled: true
      duration: PT5M
      version-source: "api:http://auth-service/version"

# JWT Configuration  
auth:
  jwt:
    private-key: |
      -----BEGIN PRIVATE KEY-----
      [Your RSA Private Key]
      -----END PRIVATE KEY-----
    public-key: |
      -----BEGIN PUBLIC KEY-----
      [Your RSA Public Key]
      -----END PUBLIC KEY-----
    access-token-time-to-live: PT15M
    refresh-token-time-to-live: PT7D

# Security - Public paths
security:
  public-paths:
    - "/api/auth/**"
    - "/api/public/**"
    - "/health"
```

## API Response Framework

### BaseResponse Structure

```java
public record BaseResponse<T>(
    Integer code,           // HTTP status code
    String message,         // Descriptive message 
    T data,                // Response data
    Pagination pagination, // Page info (optional)
    Map<String, Object> errors // Error details (optional)
)
```

### Basic Usage

```java
@RestController
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<User>> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(BaseResponse.of(user));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<User>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<User> userPage = userService.findAll(PageRequest.of(page, size));
        Pagination pagination = new Pagination(page, size, userPage.getTotalElements());
        
        return ResponseEntity.ok(BaseResponse.of(userPage.getContent(), pagination));
    }
}
```

### Factory Methods

```java
// Success response với data
BaseResponse.of(data)

// Success response với pagination 
BaseResponse.of(data, pagination)

// Response với code và message
BaseResponse.of(code, message)

// Error response từ exception
BaseResponse.of(exception)
```

## JWT Authentication

Library cung cấp services để generate và verify JWT tokens:

### Token Generation

```java
@Service
public class AuthService {

    @Autowired
    private TokenIssuerService tokenIssuer;

    public String createAccessToken(String userId, List<String> roles) {
        return tokenIssuer.generateToken(
            UUID.randomUUID().toString(), // session ID
            TokenType.ACCESS_TOKEN,
            userId,
            "username", 
            roles,
            List.of(), // groups
            900L // 15 minutes
        );
    }
}
```

### Token Verification

```java
@Service  
public class AuthService {

    @Autowired
    private TokenVerifierService tokenVerifier;

    public Token validateToken(String tokenString) {
        return tokenVerifier.verifyToken(tokenString);
    }
}
```

### Access Current User

```java
@RestController
public class ProfileController {

    @GetMapping("/profile")
    public ResponseEntity<BaseResponse<UserProfile>> getProfile(Authentication auth) {
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        
        Long userId = principal.getUserId();
        String username = principal.getUsername();
        List<Long> roles = principal.getRoles();
        
        // Your business logic here
        UserProfile profile = profileService.getProfile(userId);
        
        return ResponseEntity.ok(BaseResponse.of(profile));
    }
}
```

## Casbin Authorization

### @CasbinAuthorize Annotation

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    @CasbinAuthorize(resource = "user", action = "read")
    public ResponseEntity<BaseResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(BaseResponse.of(users));
    }

    @PostMapping
    @CasbinAuthorize(resource = "user", action = "create")
    public ResponseEntity<BaseResponse<User>> createUser(@RequestBody User user) {
        User saved = userService.save(user);
        return ResponseEntity.ok(BaseResponse.of(saved));
    }

    @DeleteMapping("/{id}")
    @CasbinAuthorize(resource = "user", action = "delete")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok(BaseResponse.of(200, CommonMessage.SUCCESS));
    }
}
```

### Policy Configuration

Library supports loading policies từ 3 sources:

```yaml
# Database source
app.casbin.policy-source: "database:SELECT role_name, resource_code, action_name FROM permissions"

# API source  
app.casbin.policy-source: "api:http://auth-service/api/policies"

# CSV file source
app.casbin.policy-source: "resource:policies.csv"
```

## Exception Handling

Library tự động handle exceptions và convert thành BaseResponse:

### Service Layer

```java
@Service
public class UserService {

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> 
                    new ResponseStatusException(HttpStatus.NOT_FOUND, CommonMessage.NOT_FOUND));
    }

    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, CommonMessage.EXISTING);
        }
        return userRepository.save(user);
    }
}
```

### CommonMessage Constants

```java
CommonMessage.SUCCESS       // "success"
CommonMessage.NOT_FOUND     // "not_found"  
CommonMessage.EXISTING      // "existing"
CommonMessage.UNAUTHORIZED  // "unauthorized"
CommonMessage.FORBIDDEN     // "forbidden"
CommonMessage.CONFLICT      // "conflict"
CommonMessage.INVALID       // "invalid"
```

## Web Utilities

### IP Address Injection

```java
@RestController
public class LogController {

    @PostMapping("/log")
    public ResponseEntity<BaseResponse<Void>> logAction(
            @IpAddress String clientIp,
            @RequestBody LogRequest request) {
        
        // clientIp tự động resolve từ X-Forwarded-For hoặc remote address
        logService.log(request.getAction(), clientIp);
        
        return ResponseEntity.ok(BaseResponse.of(200, CommonMessage.SUCCESS));
    }
}
```

### User Agent Injection

```java
@RestController  
public class AnalyticsController {

    @PostMapping("/track")
    public ResponseEntity<BaseResponse<Void>> trackEvent(
            @UserAgent String userAgent,
            @RequestBody TrackRequest request) {
        
        // userAgent tự động extract từ User-Agent header
        analyticsService.track(request.getEvent(), userAgent);
        
        return ResponseEntity.ok(BaseResponse.of(200, CommonMessage.SUCCESS));
    }
}
```

## Base Models

Library cung cấp base classes cho JPA entities:

```java
// For Long ID
@Entity
public class User extends BaseModel {
    private String name;
    private String email;
    // ID, createdAt, updatedAt inherited từ BaseModel
}

// For String ID  
@Entity
public class Session extends StringBaseModel {
    private String token;
    private LocalDateTime expiresAt;
    // String ID, createdAt, updatedAt inherited
}
```

## Lưu Ý Quan Trọng

### Scope Limitations

1. **Library chỉ cung cấp utilities** - bạn cần implement business logic
2. **Không có user storage** - cần tự tạo User entities và repositories  
3. **Không có authentication endpoints** - cần tự implement login/logout controllers
4. **RSA keys phải cung cấp externally** - library không generate keys
5. **Policy data schema** - cần tự define database tables cho Casbin

### Required Implementation

Để sử dụng đầy đủ library, bạn cần implement:

```java
// 1. User entity và repository
@Entity
public class User extends BaseModel {
    private String email;
    private String password; // hash externally
    // other fields
}

// 2. Authentication controller
@RestController
public class AuthController {
    
    @PostMapping("/login") 
    public ResponseEntity<BaseResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
        // Validate credentials
        // Generate JWT using TokenIssuerService
        // Return tokens
    }
}

// 3. Casbin policy tables
CREATE TABLE permissions (
    role_name VARCHAR(100),
    resource_code VARCHAR(100), 
    action_name VARCHAR(100)
);

// 4. RSA keys configuration
auth.jwt.private-key: [Generated RSA private key]
auth.jwt.public-key: [Generated RSA public key]
```

## Best Practices

1. **Luôn sử dụng BaseResponse** cho tất cả API endpoints
2. **Sử dụng CommonMessage constants** thay vì hardcode strings  
3. **Throw ResponseStatusException** trong service layer cho proper error handling
4. **Configure public paths** để bypass authentication cho public endpoints
5. **Use @CasbinAuthorize** cho method-level authorization
6. **Store RSA keys securely** - không commit vào source code