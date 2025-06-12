# Web Utilities - Hướng Dẫn Chi Tiết

Tài liệu này mô tả cách sử dụng `@IpAddress` và `@UserAgent` annotations để extract thông tin từ HTTP requests.

## Tổng Quan

Library cung cấp hai annotations để tự động inject thông tin từ HTTP request vào controller parameters:

✅ **@IpAddress** - Extract client IP address từ headers hoặc remote address  
✅ **@UserAgent** - Extract User-Agent header  
✅ **IpAddressArgumentResolver** - Automatic IP resolution  
✅ **UserAgentArgumentResolver** - Automatic User-Agent extraction  

❌ **Library KHÔNG cung cấp**: IP geo-location, user agent parsing, analytics tracking, session management

## @IpAddress Annotation

### Tổng Quan

`@IpAddress` annotation tự động inject client IP address vào String parameters.

### IP Resolution Priority

1. **X-Forwarded-For header** - Cho proxy/load balancer scenarios
2. **Remote address** - Direct connection IP
3. **"unknown"** - Nếu không determine được IP

### Basic Usage

```java
@RestController
@RequestMapping("/api/logs")
public class LogController {

    @PostMapping("/access")
    public ResponseEntity<BaseResponse<Void>> logAccess(
            @IpAddress String clientIp,
            @RequestBody LogRequest request) {
        
        // clientIp automatically resolved
        System.out.println("Request from IP: " + clientIp);
        
        // Your logging logic here
        logService.logAccess(request.getAction(), clientIp);
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS)
        );
    }

    @GetMapping("/user-sessions")
    public ResponseEntity<BaseResponse<List<SessionInfo>>> getUserSessions(
            @IpAddress String currentIp,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        // Your business logic - get user sessions
        List<SessionInfo> sessions = sessionService.getUserSessions(principal.getUserId());
        
        // Mark current session based on IP
        sessions.forEach(session -> {
            if (session.getIpAddress().equals(currentIp)) {
                session.setCurrent(true);
            }
        });
        
        return ResponseEntity.ok(BaseResponse.of(sessions));
    }
}
```

### Advanced IP Usage

```java
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @IpAddress String clientIp) {
        
        // Check for suspicious IPs (your implementation)
        if (securityService.isSuspiciousIp(clientIp)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "Login from this IP is temporarily blocked"
            );
        }

        // Authenticate user
        TokenResponse tokens = authService.login(request);
        
        // Log successful login with IP
        auditService.logLogin(request.getEmail(), clientIp, true);
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS, tokens)
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @IpAddress String clientIp,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        // Change password (your implementation)
        userService.changePassword(principal.getUserId(), request);
        
        // Log security event with IP
        SecurityEvent event = new SecurityEvent();
        event.setUserId(principal.getUserId());
        event.setEventType("PASSWORD_CHANGE");
        event.setIpAddress(clientIp);
        event.setTimestamp(LocalDateTime.now());
        
        securityService.logEvent(event);
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS)
        );
    }
}
```

## @UserAgent Annotation

### Tổng Quan

`@UserAgent` annotation tự động inject User-Agent header vào String parameters.

### Basic Usage

```java
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @PostMapping("/page-view")
    public ResponseEntity<BaseResponse<Void>> trackPageView(
            @Valid @RequestBody PageViewRequest request,
            @UserAgent String userAgent,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        // Track page view với user agent info
        PageView pageView = new PageView();
        pageView.setUserId(principal.getUserId());
        pageView.setPageUrl(request.getPageUrl());
        pageView.setUserAgent(userAgent);
        pageView.setTimestamp(LocalDateTime.now());
        
        analyticsService.trackPageView(pageView);
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS)
        );
    }

    @PostMapping("/event")
    public ResponseEntity<BaseResponse<Void>> trackEvent(
            @Valid @RequestBody EventRequest request,
            @UserAgent String userAgent,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        // Track custom event
        AnalyticsEvent event = new AnalyticsEvent();
        event.setUserId(principal.getUserId());
        event.setEventName(request.getEventName());
        event.setUserAgent(userAgent);
        event.setProperties(request.getProperties());
        event.setTimestamp(LocalDateTime.now());
        
        analyticsService.trackEvent(event);
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS)
        );
    }
}
```

## Combined Usage Examples

### Comprehensive Request Logging

```java
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @PostMapping("/action")
    public ResponseEntity<BaseResponse<Void>> auditAction(
            @Valid @RequestBody AuditActionRequest request,
            @IpAddress String clientIp,
            @UserAgent String userAgent,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        // Create audit log entry
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(principal.getUserId());
        auditLog.setUsername(principal.getUsername());
        auditLog.setAction(request.getAction());
        auditLog.setResource(request.getResource());
        auditLog.setResourceId(request.getResourceId());
        auditLog.setIpAddress(clientIp);
        auditLog.setUserAgent(userAgent);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetails(request.getDetails());
        
        auditService.log(auditLog);
        
        return ResponseEntity.ok(
            BaseResponse.of(HttpStatus.OK.value(), CommonMessage.SUCCESS)
        );
    }

    @GetMapping("/user-activity")
    public ResponseEntity<BaseResponse<List<AuditLog>>> getUserActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getUserActivity(principal.getUserId(), pageable);
        
        Pagination pagination = new Pagination(
            auditLogs.getNumber(),
            auditLogs.getSize(),
            auditLogs.getTotalElements()
        );
        
        return ResponseEntity.ok(BaseResponse.of(auditLogs.getContent(), pagination));
    }
}
```

### Basic Device Tracking

```java
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<SessionInfo>> createSession(
            @IpAddress String clientIp,
            @UserAgent String userAgent,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        // Create session info (your implementation)
        SessionInfo session = new SessionInfo();
        session.setUserId(principal.getUserId());
        session.setIpAddress(clientIp);
        session.setUserAgent(userAgent);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastAccessAt(LocalDateTime.now());
        
        SessionInfo saved = sessionService.save(session);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.of(HttpStatus.CREATED.value(), CommonMessage.SUCCESS, saved));
    }

    @GetMapping("/active")
    public ResponseEntity<BaseResponse<List<SessionInfo>>> getActiveSessions(
            @IpAddress String currentIp,
            @UserAgent String currentUserAgent,
            Authentication authentication) {
        
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        List<SessionInfo> sessions = sessionService.getActiveSessions(principal.getUserId());
        
        // Mark current session
        sessions.forEach(session -> {
            if (session.getIpAddress().equals(currentIp) && 
                session.getUserAgent().equals(currentUserAgent)) {
                session.setCurrent(true);
            }
        });
        
        return ResponseEntity.ok(BaseResponse.of(sessions));
    }
}
```

## Configuration

### Register Argument Resolvers (Required)

Library components tự động register, nhưng bạn cần ensure WebMvc configuration:

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private IpAddressArgumentResolver ipAddressArgumentResolver;
    
    @Autowired
    private UserAgentArgumentResolver userAgentArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(ipAddressArgumentResolver);
        resolvers.add(userAgentArgumentResolver);
    }
}
```

## Data Models Examples

### Simple Audit Log Entity

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseModel {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "resource")
    private String resource;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    // constructors, getters, setters
    // ID, createdAt, updatedAt inherited từ BaseModel
}
```

### Session Info Entity

```java
@Entity
@Table(name = "user_sessions")
public class SessionInfo extends BaseModel {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_access_at", nullable = false)
    private LocalDateTime lastAccessAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Transient
    private Boolean current = false; // For marking current session

    // constructors, getters, setters
}
```

## Request/Response DTOs

```java
public class LogRequest {
    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Resource is required")
    private String resource;

    private String resourceId;
    private String details;

    // getters, setters
}

public class AuditActionRequest {
    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Resource is required")
    private String resource;

    private String resourceId;
    private String details;

    // getters, setters
}

public class PageViewRequest {
    @NotBlank(message = "Page URL is required")
    private String pageUrl;

    private String referrer;
    private Map<String, Object> metadata;

    // getters, setters
}

public class EventRequest {
    @NotBlank(message = "Event name is required")
    private String eventName;

    private Map<String, Object> properties;

    // getters, setters
}
```

## Testing Web Utilities

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WebUtilitiesTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testIpAddressExtraction() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Forwarded-For", "192.168.1.100");
        headers.setBearerAuth(getTestToken());

        LogRequest request = new LogRequest();
        request.setAction("test_action");
        request.setResource("test_resource");

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/logs/access",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Verify IP was logged correctly in your implementation
    }

    @Test
    void testUserAgentExtraction() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 Chrome/91.0");
        headers.setBearerAuth(getTestToken());

        PageViewRequest request = new PageViewRequest();
        request.setPageUrl("/dashboard");

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/analytics/page-view",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Verify User-Agent was tracked correctly
    }
}
```

## Best Practices

### 1. IP Address Handling
- Validate và sanitize IP addresses before storing
- Handle edge cases ("unknown", IPv6 localhost)
- Consider privacy regulations when storing IPs
- Implement IP-based security measures carefully

### 2. User Agent Processing
- Handle null/empty User-Agent strings gracefully
- Truncate very long User-Agent strings
- Don't rely on User-Agent for critical security decisions
- Store raw User-Agent string for future processing

### 3. Data Storage
- Index IP address columns for performance
- Implement data retention policies
- Consider data anonymization requirements
- Use appropriate column sizes

### 4. Security Considerations
- Log suspicious IP activities
- Implement rate limiting per IP if needed
- Monitor for unusual User-Agent patterns
- Validate input data thoroughly

### 5. Performance
- Batch insert audit logs for high-volume applications
- Use async processing cho analytics data
- Consider using time-series databases cho large datasets
- Implement proper database indexing

## Library Limitations

### What Library Provides
- Basic IP address extraction từ headers
- User-Agent header extraction  
- Automatic parameter injection
- Spring MVC integration

### What You Need To Implement
- IP geo-location lookup
- User-Agent parsing (browser, OS, device detection)
- Analytics data processing
- Security monitoring logic
- Data storage và retention
- Privacy compliance features

## Example Implementation

```java
// Your service implementation example
@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAccess(String action, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
    }

    public Page<AuditLog> getUserActivity(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }
}

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    List<AuditLog> findByIpAddressAndTimestampAfter(String ipAddress, LocalDateTime after);
}
```

Library chỉ cung cấp basic parameter injection - business logic, data processing, và analytics phải tự implement.