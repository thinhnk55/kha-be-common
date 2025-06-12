# Casbin Authorization - Hướng Dẫn Chi Tiết

Tài liệu này mô tả cách sử dụng Casbin RBAC integration trong library.

## Tổng Quan Casbin Features

Library cung cấp:

✅ **@CasbinAuthorize** annotation cho method-level authorization  
✅ **PolicyLoader** - Load policies từ multiple sources (database, API, CSV)  
✅ **VersionPollingService** - Background polling cho policy updates  
✅ **CasbinAuthorizeAspect** - AOP aspect enforce permissions  
✅ **PolicyRule** entity cho RBAC rules  

❌ **Library KHÔNG cung cấp**: User role management, policy management endpoints, Casbin model file, database schema

## @CasbinAuthorize Annotation

### Basic Usage

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

    @PutMapping("/{id}")
    @CasbinAuthorize(resource = "user", action = "update")
    public ResponseEntity<BaseResponse<User>> updateUser(
            @PathVariable Long id, @RequestBody User user) {
        User updated = userService.update(id, user);
        return ResponseEntity.ok(BaseResponse.of(updated));
    }

    @DeleteMapping("/{id}")
    @CasbinAuthorize(resource = "user", action = "delete")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok(BaseResponse.of(200, CommonMessage.SUCCESS));
    }
}
```

### Class-Level Authorization

```java
@RestController
@RequestMapping("/api/admin")
@CasbinAuthorize(resource = "admin", action = "access")
public class AdminController {

    // Tất cả methods require admin access

    @GetMapping("/users")
    @CasbinAuthorize(resource = "user", action = "read") // Override class-level
    public ResponseEntity<BaseResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(BaseResponse.of(users));
    }
}
```

### Fine-Grained Permissions

```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @GetMapping
    @CasbinAuthorize(resource = "document", action = "list")
    public ResponseEntity<BaseResponse<List<Document>>> getDocuments() {
        // List documents
    }

    @GetMapping("/{id}")
    @CasbinAuthorize(resource = "document", action = "read")
    public ResponseEntity<BaseResponse<Document>> getDocument(@PathVariable Long id) {
        // Read specific document
    }

    @PostMapping("/{id}/publish")
    @CasbinAuthorize(resource = "document", action = "publish")
    public ResponseEntity<BaseResponse<Void>> publishDocument(@PathVariable Long id) {
        // Publish document - restricted action
    }
}
```

## Policy Configuration

### Application Properties

```yaml
app:
  casbin:
    # Policy source - choose one:
    policy-source: "database:SELECT role_name, resource_code, action_name FROM permissions WHERE is_active = true"
    
    # Resource filtering cho microservices
    resources:
      - "user"
      - "document" 
      - "order"
    
    # Version polling configuration
    polling:
      enabled: true
      duration: PT5M  # Check every 5 minutes
      version-source: "api:http://auth-service/api/internal/version/policy_version"
```

### Policy Sources

#### 1. Database Source

```yaml
app:
  casbin:
    policy-source: "database:SELECT role_name, resource_code, action_name FROM permissions WHERE is_active = true"
```

Required database table:
```sql
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    resource_code VARCHAR(100) NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample data
INSERT INTO permissions (role_name, resource_code, action_name) VALUES
('admin', 'user', 'read'),
('admin', 'user', 'create'),
('admin', 'user', 'update'),
('admin', 'user', 'delete'),
('manager', 'user', 'read'),
('manager', 'user', 'create'),
('user', 'profile', 'read'),
('user', 'profile', 'update');
```

#### 2. API Source

```yaml
app:
  casbin:
    policy-source: "api:http://auth-service/api/internal/policies"
```

API response format:
```json
{
  "data": [
    {
      "subject": "admin",
      "object": "user", 
      "action": "read"
    },
    {
      "subject": "admin",
      "object": "user",
      "action": "create"
    }
  ]
}
```

#### 3. Resource File Source

```yaml
app:
  casbin:
    policy-source: "resource:casbin/policies.csv"
```

File `src/main/resources/casbin/policies.csv`:
```csv
p,admin,user,read
p,admin,user,create
p,admin,user,update
p,admin,user,delete
p,manager,user,read
p,manager,user,create
p,user,profile,read
p,user,profile,update
```

## Required Setup

### 1. Casbin Model File

Tạo `src/main/resources/casbin/model.conf`:

```ini
[request_definition]
r = sub, obj, act

[policy_definition]
p = sub, obj, act

[role_definition]
g = _, _

[policy_effect]
e = some(where (p.eft == allow))

[matchers]
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
```

### 2. Casbin Configuration

```java
@Configuration
public class CasbinConfig {

    @Bean
    public Enforcer casbin() {
        try {
            String modelPath = "classpath:casbin/model.conf";
            return new Enforcer(modelPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Casbin", e);
        }
    }
}
```

### 3. Role Integration với JWT

Roles phải được include trong JWT tokens:

```java
@Service
public class AuthService {

    public String generateTokenWithRoles(User user) {
        // Load user roles (your implementation)
        List<String> roles = getUserRoles(user.getId());
        
        return tokenIssuer.generateToken(
            UUID.randomUUID().toString(),
            TokenType.ACCESS_TOKEN,
            user.getId().toString(),
            user.getEmail(),
            roles,  // Important: roles for Casbin
            List.of(),
            900L
        );
    }
    
    private List<String> getUserRoles(Long userId) {
        // Your implementation - load from database
        return userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.toList());
    }
}
```

### 4. User Role Management

Bạn cần implement user role assignment:

```java
@Entity
@Table(name = "user_roles")
public class UserRole extends BaseModel {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "role_name", nullable = false)
    private String roleName;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // constructors, getters, setters
}

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserIdAndIsActiveTrue(Long userId);
    boolean existsByUserIdAndRoleNameAndIsActiveTrue(Long userId, String roleName);
}
```

## Policy Examples

### Basic RBAC Policies

```csv
# Format: p, role, resource, action

# Admin permissions - full access
p, admin, user, read
p, admin, user, create
p, admin, user, update
p, admin, user, delete
p, admin, document, read
p, admin, document, create
p, admin, document, update
p, admin, document, delete
p, admin, document, publish

# Manager permissions - limited admin
p, manager, user, read
p, manager, user, create
p, manager, document, read
p, manager, document, create
p, manager, document, update

# User permissions - basic access
p, user, profile, read
p, user, profile, update
p, user, document, read
```

### Department-Based Permissions

```csv
# HR department
p, hr_manager, employee, read
p, hr_manager, employee, create
p, hr_manager, employee, update
p, hr_staff, employee, read

# Finance department  
p, finance_manager, invoice, read
p, finance_manager, invoice, create
p, finance_manager, invoice, approve
p, finance_staff, invoice, read

# IT department
p, it_admin, server, read
p, it_admin, server, restart
p, it_admin, user_account, create
p, it_admin, user_account, disable
```

## Version Polling

Library supports automatic policy reload:

### Database Version Tracking

```sql
CREATE TABLE policy_versions (
    id BIGINT PRIMARY KEY,
    version_name VARCHAR(100) NOT NULL UNIQUE,
    version_value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO policy_versions (version_name, version_value) 
VALUES ('policy_version', '1.0.0');
```

### Version Source Configuration

```yaml
app:
  casbin:
    polling:
      enabled: true
      duration: PT5M
      # Database version source
      version-source: "database:SELECT version_value FROM policy_versions WHERE version_name = 'policy_version'"
      
      # API version source
      # version-source: "api:http://auth-service/api/internal/version/policy_version"
```

### Manual Policy Reload

```java
@Service
public class PolicyManagementService {

    @Autowired
    private PolicyLoader policyLoader;
    
    @Autowired
    private Enforcer enforcer;

    public void reloadPolicies() {
        policyLoader.loadPolicies(enforcer);
    }
    
    public void updatePolicyVersion(String newVersion) {
        // Update version in database
        policyVersionRepository.updateVersion("policy_version", newVersion);
        
        // Trigger reload
        reloadPolicies();
    }
}
```

## Programmatic Authorization Check

```java
@Service
public class AuthorizationService {

    @Autowired
    private Enforcer enforcer;

    public boolean hasPermission(String role, String resource, String action) {
        return enforcer.enforce(role, resource, action);
    }

    public boolean hasAnyPermission(List<String> roles, String resource, String action) {
        return roles.stream()
                .anyMatch(role -> enforcer.enforce(role, resource, action));
    }

    public List<String> getAllowedActions(String role, String resource) {
        List<String> actions = Arrays.asList("read", "create", "update", "delete");
        return actions.stream()
                .filter(action -> enforcer.enforce(role, resource, action))
                .collect(Collectors.toList());
    }
}
```

### Usage trong Service Layer

```java
@Service
public class DocumentService {

    @Autowired
    private AuthorizationService authService;

    public List<Document> getDocuments(Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Check permissions
        if (!authService.hasAnyPermission(roles, "document", "read")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, CommonMessage.FORBIDDEN);
        }

        // Return data based on permissions
        if (authService.hasAnyPermission(roles, "document", "admin")) {
            return documentRepository.findAll(); // Admin sees all
        } else {
            return documentRepository.findByOwnerId(principal.getUserId()); // User sees own only
        }
    }
}
```

## Testing Authorization

```java
@SpringBootTest
class CasbinAuthorizationTest {

    @Autowired
    private Enforcer enforcer;

    @Test
    void testAdminCanAccessUsers() {
        boolean result = enforcer.enforce("admin", "user", "read");
        assertTrue(result);
    }

    @Test
    void testUserCannotDeleteUsers() {
        boolean result = enforcer.enforce("user", "user", "delete");
        assertFalse(result);
    }

    @Test
    void testManagerCanCreateDocuments() {
        boolean result = enforcer.enforce("manager", "document", "create");
        assertTrue(result);
    }
}
```

### Integration Test

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthorizationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testUserCanAccessProfile() {
        String token = generateTestToken("user", List.of("user"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/profile",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUserCannotAccessAdminEndpoint() {
        String token = generateTestToken("user", List.of("user"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/admin/users",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
```

## Best Practices

### 1. Policy Design
- Keep resource và action names simple và consistent
- Use lowercase naming (user, document, read, create)
- Group related permissions logically
- Avoid overly fine-grained permissions

### 2. Role Management
- Create hierarchical roles khi có thể
- Use meaningful role names (admin, manager, user)
- Regularly audit role assignments
- Implement principle of least privilege

### 3. Performance
- Use resource filtering cho microservices
- Enable version polling để avoid unnecessary reloads
- Cache permission checks khi có thể
- Monitor policy loading performance

### 4. Security
- Regularly audit policy rules
- Log authorization failures
- Use secure channels cho policy APIs
- Validate policy data integrity

### 5. Implementation Requirements

Để sử dụng Casbin authorization, bạn PHẢI implement:

```java
// 1. Casbin model file
// Create src/main/resources/casbin/model.conf

// 2. Policy storage (choose one)
// Database: CREATE TABLE permissions(...)
// API: Implement policy endpoint
// File: Create CSV policy file  

// 3. User role management
@Entity
public class UserRole {
    private Long userId;
    private String roleName;
    // other fields
}

// 4. Role loading trong JWT generation
public List<String> getUserRoles(Long userId) {
    // Your implementation
}

// 5. Casbin Enforcer bean configuration
@Bean
public Enforcer casbin() {
    return new Enforcer("classpath:casbin/model.conf");
}
```

Library chỉ cung cấp annotation và policy loading utilities - role management và policy data phải tự implement.