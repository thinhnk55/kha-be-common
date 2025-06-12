# Security và JWT - Hướng Dẫn Chi Tiết

Tài liệu này mô tả cách sử dụng JWT authentication services và security components trong library.

## Tổng Quan JWT Services

Library cung cấp:

✅ **TokenIssuerService** - Generate JWT tokens với RS256 signing  
✅ **TokenVerifierService** - Parse và validate JWT tokens  
✅ **CustomUserPrincipal** - Spring Security UserDetails implementation  
✅ **JwtTokenFilter** - Spring Security filter cho JWT processing  
✅ **RSAKeyUtil** - Load RSA keys từ PEM format  

❌ **Library KHÔNG cung cấp**: Authentication endpoints, user storage, password hashing, session management

## JWT Token Services

### TokenIssuerService - Generate Tokens

```java
@Service
public class AuthService {

    @Autowired
    private TokenIssuerService tokenIssuer;

    public String generateAccessToken(String userId, String username, List<String> roles) {
        return tokenIssuer.generateToken(
            UUID.randomUUID().toString(), // sessionId
            TokenType.ACCESS_TOKEN,
            userId,                       // subjectID
            username,                     // subjectName
            roles,                        // roles
            List.of(),                    // groups (optional)
            900L                          // timeToLive in seconds (15 minutes)
        );
    }

    public String generateRefreshToken(String userId, String username, List<String> roles) {
        return tokenIssuer.generateToken(
            UUID.randomUUID().toString(),
            TokenType.REFRESH_TOKEN,
            userId,
            username,
            roles,
            List.of(),
            604800L                       // 7 days
        );
    }

    public String refreshAccessToken(Token refreshToken) {
        return tokenIssuer.refreshToken(refreshToken, 900); // 15 minutes
    }
}
```

### TokenVerifierService - Validate Tokens

```java
@Service
public class AuthService {

    @Autowired
    private TokenVerifierService tokenVerifier;

    public Token validateToken(String tokenString) {
        try {
            return tokenVerifier.verifyToken(tokenString);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, 
                CommonMessage.UNAUTHORIZED
            );
        }
    }

    public boolean isTokenValid(String tokenString) {
        try {
            tokenVerifier.verifyToken(tokenString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Authentication Controller Implementation

Bạn cần tự implement authentication endpoints:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private TokenIssuerService tokenIssuer;
    @Autowired
    private UserService userService; // Your implementation

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        // Validate user credentials (your implementation)
        User user = userService.validateCredentials(request.getEmail(), request.getPassword());
        
        // Load user roles (your implementation)
        List<String> roles = userService.getUserRoles(user.getId());
        
        // Generate tokens using library services
        String accessToken = tokenIssuer.generateToken(
            UUID.randomUUID().toString(),
            TokenType.ACCESS_TOKEN,
            user.getId().toString(),
            user.getEmail(),
            roles,
            List.of(),
            900L // 15 minutes
        );

        String refreshToken = tokenIssuer.generateToken(
            UUID.randomUUID().toString(),
            TokenType.REFRESH_TOKEN,
            user.getId().toString(),
            user.getEmail(),
            roles,
            List.of(),
            604800L // 7 days
        );

        TokenResponse response = new TokenResponse(accessToken, refreshToken);
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS, response)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        // Verify refresh token
        Token refreshToken = tokenVerifier.verifyToken(request.getRefreshToken());
        
        if (refreshToken.getTokenType() != TokenType.REFRESH_TOKEN) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Invalid token type"
            );
        }

        // Generate new access token
        String newAccessToken = tokenIssuer.refreshToken(refreshToken, 900);
        
        TokenResponse response = new TokenResponse(newAccessToken, request.getRefreshToken());
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS, response)
        );
    }
}
```

### Request/Response DTOs

```java
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    // getters, setters
}

public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn = 900; // 15 minutes

    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // getters, setters
}

public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // getters, setters
}
```

## Accessing Current User Information

Library cung cấp `CustomUserPrincipal` để access user info:

```java
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @GetMapping
    public ResponseEntity<BaseResponse<UserProfile>> getCurrentUser(Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        // Access user information
        Long sessionId = principal.getSessionId();
        Long userId = principal.getUserId();
        String username = principal.getUsername();
        List<Long> roles = principal.getRoles();
        List<Long> groups = principal.getGroups();
        
        // Your business logic
        UserProfile profile = profileService.getProfile(userId);
        
        return ResponseEntity.ok(BaseResponse.of(profile));
    }

    @PutMapping
    public ResponseEntity<BaseResponse<UserProfile>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        UserProfile profile = profileService.updateProfile(principal.getUserId(), request);
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS, profile)
        );
    }
}
```

## Configuration

### JWT Properties

```yaml
# application.yml
auth:
  jwt:
    private-key: |
      -----BEGIN PRIVATE KEY-----
      MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC...
      -----END PRIVATE KEY-----
    public-key: |
      -----BEGIN PUBLIC KEY-----
      MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtF...
      -----END PUBLIC KEY-----
    paraphrase: ""  # Optional passphrase for private key
    access-token-time-to-live: PT15M    # 15 minutes
    refresh-token-time-to-live: PT7D    # 7 days

# Security paths
security:
  public-paths:
    - "/api/auth/**"
    - "/api/public/**"
    - "/health"
    - "/actuator/health"
```

### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityProperties securityProperties;
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String[] publicPaths = securityProperties.getPublicPaths()
                .toArray(new String[0]);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> 
                exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(publicPaths).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## RSA Key Management

### Generate RSA Keys

```bash
# Generate private key
openssl genrsa -out private.pem 2048

# Extract public key
openssl rsa -in private.pem -pubout -out public.pem

# Convert to PKCS#8 format (required for library)
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private.pem -out private_pkcs8.pem
```

### Load Keys Programmatically

```java
@Configuration
public class JwtConfig {

    @Bean
    public KeyPair keyPair(@Value("${auth.jwt.private-key}") String privateKey,
                          @Value("${auth.jwt.public-key}") String publicKey,
                          @Value("${auth.jwt.paraphrase:}") String paraphrase) {
        try {
            if (paraphrase.isEmpty()) {
                return RSAKeyUtil.loadKeyPair(privateKey, publicKey);
            } else {
                return RSAKeyUtil.loadKeyPair(privateKey, publicKey, paraphrase);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT keys", e);
        }
    }
}
```

## Token Structure

Library tạo JWT tokens với structure sau:

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sessionId": "uuid-session-id",
    "sub": "user-id", 
    "sub_name": "username",
    "sub_type": "USER",
    "roles": ["admin", "user"],
    "groups": [],
    "type": "ACCESS_TOKEN",
    "iat": 1640995200,
    "exp": 1640999800
  }
}
```

## Error Handling

Library provides automatic error handling:

### Authentication Errors

```java
// JwtAuthenticationEntryPoint tự động handle authentication failures
// Returns BaseResponse format:
{
  "code": 401,
  "message": "unauthorized"
}
```

### Token Validation Errors

```java
@Service
public class AuthService {
    
    public Token validateToken(String tokenString) {
        try {
            return tokenVerifier.verifyToken(tokenString);
        } catch (Exception e) {
            // Handle specific token errors
            if (e.getMessage().contains("expired")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");
            } else if (e.getMessage().contains("signature")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token signature");
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CommonMessage.UNAUTHORIZED);
            }
        }
    }
}
```

## Testing JWT Services

```java
@SpringBootTest
class JwtServiceTest {

    @Autowired
    private TokenIssuerService tokenIssuer;
    
    @Autowired
    private TokenVerifierService tokenVerifier;

    @Test
    void testTokenGenerationAndVerification() {
        // Generate token
        String token = tokenIssuer.generateToken(
            "session-123",
            TokenType.ACCESS_TOKEN,
            "user-456",
            "testuser",
            List.of("user", "admin"),
            List.of(),
            3600L
        );

        assertNotNull(token);

        // Verify token
        Token verifiedToken = tokenVerifier.verifyToken(token);
        
        assertEquals("session-123", verifiedToken.getSessionId());
        assertEquals("user-456", verifiedToken.getSubjectId());
        assertEquals("testuser", verifiedToken.getSubjectName());
        assertEquals(TokenType.ACCESS_TOKEN, verifiedToken.getTokenType());
        assertEquals(List.of("user", "admin"), verifiedToken.getRoles());
    }

    @Test
    void testExpiredToken() {
        // Generate token với short expiry
        String token = tokenIssuer.generateToken(
            "session-123",
            TokenType.ACCESS_TOKEN,
            "user-456",
            "testuser",
            List.of("user"),
            List.of(),
            1L // 1 second
        );

        // Wait for expiry
        Thread.sleep(2000);

        // Verify throws exception
        assertThrows(Exception.class, () -> {
            tokenVerifier.verifyToken(token);
        });
    }
}
```

## Best Practices

### 1. Token Security
- Store RSA keys securely (environment variables, not in code)
- Use appropriate token expiration times (15-30 minutes for access, 7 days for refresh)
- Implement token revocation if needed
- Validate tokens on every request

### 2. Error Handling
- Don't expose detailed token errors to clients
- Log token validation failures for security monitoring
- Use standard HTTP status codes

### 3. Performance
- Cache public keys for token verification
- Use async token generation for bulk operations
- Consider token pooling for high-throughput scenarios

### 4. Implementation Requirements

Để sử dụng JWT services, bạn PHẢI implement:

```java
// 1. User entity and repository
@Entity
public class User extends BaseModel {
    private String email;
    private String password; // Use BCrypt or similar
    // other fields
}

// 2. User service with credential validation
@Service
public class UserService {
    public User validateCredentials(String email, String password) {
        // Your implementation
    }
    
    public List<String> getUserRoles(Long userId) {
        // Your implementation  
    }
}

// 3. RSA key pair configuration
// Generate and configure in application.yml

// 4. Security filter chain configuration
// Enable JWT filter in Spring Security
```

Library chỉ cung cấp JWT token utilities - business logic và data storage phải tự implement.