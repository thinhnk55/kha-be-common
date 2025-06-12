# Configuration Setup - Hướng Dẫn Chi Tiết

Tài liệu này mô tả cách cấu hình library với các properties và setup được support.

## Dependency Setup

### Gradle Configuration

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.thinhnk55:kha-be-common:1.0.8")
    
    // Required Spring Boot dependencies (nếu chưa có)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

### Maven Configuration

```xml
<!-- pom.xml -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.thinhnk55</groupId>
        <artifactId>kha-be-common</artifactId>
        <version>1.0.8</version>
    </dependency>
</dependencies>
```

## Application Configuration

### Main Application Class

```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.defi.common",      // Library components
    "com.yourcompany.app"   // Your application components
})
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

## Supported Configuration Properties

### Complete application.yml

```yaml
# JWT Configuration - auth.jwt.*
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
    paraphrase: ""  # Optional - passphrase for private key
    access-token-time-to-live: PT15M    # Duration - default 15 minutes
    refresh-token-time-to-live: PT7D    # Duration - default 7 days

# CORS Configuration - app.cors.*
app:
  cors:
    origins:  # List<String> - ONLY origins are supported
      - "http://localhost:3000"
      - "http://localhost:8080"
      - "https://yourdomain.com"

  # Casbin Configuration - app.casbin.*
  casbin:
    policy-source: "database:SELECT role_name, resource_code, action_name FROM permissions WHERE is_active = true"
    resources:  # List<String> - for filtering in microservices
      - "user"
      - "document"
      - "order"
    polling:
      enabled: true           # boolean - enable version polling
      duration: PT5M          # Duration - minimum PT1M (1 minute)
      version-source: "api:http://auth-service/api/internal/version/policy_version"

# Security Configuration - security.*
security:
  public-paths:  # List<String> - Ant-style patterns
    - "/api/auth/**"
    - "/api/public/**"
    - "/health"
    - "/actuator/health"
    - "/swagger-ui/**"
    - "/v3/api-docs/**"
```

## Configuration Properties Details

### 1. JWT Configuration (`auth.jwt`)

```yaml
auth:
  jwt:
    private-key: string       # Required - RSA private key in PEM format
    public-key: string        # Required - RSA public key in PEM format  
    paraphrase: string        # Optional - private key passphrase
    access-token-time-to-live: Duration   # Optional - default PT15M
    refresh-token-time-to-live: Duration  # Optional - default PT7D
```

**Lưu ý**: RSA keys phải generate externally và provide đúng format.

### 2. CORS Configuration (`app.cors`)

```yaml
app:
  cors:
    origins: List<String>     # Required - danh sách allowed origins
```

**Lưu ý**: Library CHỈ support `origins`. Không có `methods`, `headers`, `credentials`, etc.

### 3. Casbin Configuration (`app.casbin`)

```yaml
app:
  casbin:
    policy-source: string     # Required - policy source configuration
    resources: List<String>   # Optional - resource filtering
    polling:
      enabled: boolean        # Optional - default false
      duration: Duration      # Required if enabled - minimum PT1M
      version-source: string  # Required if enabled
```

#### Policy Source Formats:
- Database: `"database:SELECT role_name, resource_code, action_name FROM permissions"`
- API: `"api:http://service/api/policies"`  
- Resource: `"resource:casbin/policies.csv"`

#### Version Source Formats:
- Database: `"database:SELECT version FROM policy_versions WHERE name = 'policy'"`
- API: `"api:http://service/api/version/policy"`

### 4. Security Configuration (`security`)

```yaml
security:
  public-paths: List<String>  # Required - Ant-style URL patterns
```

## Environment-Specific Configuration

### application-dev.yml
```yaml
auth:
  jwt:
    access-token-time-to-live: PT30M  # Longer for development

app:
  cors:
    origins:
      - "http://localhost:3000"
      - "http://localhost:8080"
  
  casbin:
    polling:
      enabled: false  # Disable polling in development

security:
  public-paths:
    - "/api/**"  # More permissive for development
```

### application-prod.yml
```yaml
auth:
  jwt:
    access-token-time-to-live: PT15M  # Short for production
    refresh-token-time-to-live: PT7D

app:
  cors:
    origins:
      - "https://yourdomain.com"
      - "https://app.yourdomain.com"
  
  casbin:
    polling:
      enabled: true
      duration: PT5M

security:
  public-paths:
    - "/api/auth/**"
    - "/health"
```

## Required Spring Configuration

### Enable Library Components

```java
@Configuration
@EnableConfigurationProperties({
    CasbinProperties.class,
    SecurityProperties.class,
    CorsProperties.class
})
public class AppConfig {
    // Additional beans if needed
}
```

### Web MVC Configuration (Required)

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

### CORS Configuration (Optional)

```java
@Configuration
public class CorsConfig {

    @Autowired
    private CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Library chỉ support origins
        configuration.setAllowedOrigins(corsProperties.getOrigins());
        
        // Các settings khác phải configure manually
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

## Required External Setup

### 1. RSA Key Generation

```bash
# Generate private key
openssl genrsa -out private.pem 2048

# Extract public key  
openssl rsa -in private.pem -pubout -out public.pem

# Convert to PKCS#8 format (required)
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private.pem -out private_pkcs8.pem
```

### 2. Database Tables (If Using Database Sources)

#### Casbin Permissions Table
```sql
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    role_name VARCHAR(100) NOT NULL,
    resource_code VARCHAR(100) NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permissions_role ON permissions(role_name);
CREATE INDEX idx_permissions_resource ON permissions(resource_code);
```

#### Version Tracking Table (If Using Polling)
```sql
CREATE TABLE policy_versions (
    id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    version_name VARCHAR(100) NOT NULL UNIQUE,
    version_value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO policy_versions (version_name, version_value) 
VALUES ('policy_version', '1.0.0');
```

### 3. Casbin Model File

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

## Environment Variables

### Required Environment Variables

```bash
# JWT Keys (production)
AUTH_JWT_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----"
AUTH_JWT_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----"

# Database (if using database policy source)
DB_URL=jdbc:postgresql://localhost:5432/your_db
DB_USERNAME=your_username  
DB_PASSWORD=your_password

# CORS Origins
CORS_ORIGINS=https://yourdomain.com,https://app.yourdomain.com

# Casbin Policy Source
CASBIN_POLICY_SOURCE="database:SELECT role_name, resource_code, action_name FROM permissions"
CASBIN_VERSION_SOURCE="api:http://auth-service/api/version/policy"
```

### Environment Configuration Example

```yaml
# application.yml với environment variables
auth:
  jwt:
    private-key: ${AUTH_JWT_PRIVATE_KEY}
    public-key: ${AUTH_JWT_PUBLIC_KEY}

app:
  cors:
    origins: ${CORS_ORIGINS:http://localhost:3000}
  
  casbin:
    policy-source: ${CASBIN_POLICY_SOURCE:resource:casbin/policies.csv}
    polling:
      enabled: ${CASBIN_POLLING_ENABLED:false}
      version-source: ${CASBIN_VERSION_SOURCE:}

security:
  public-paths: ${SECURITY_PUBLIC_PATHS:/api/auth/**,/health}
```

## Docker Configuration

### docker-compose.yml

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - AUTH_JWT_PRIVATE_KEY=${JWT_PRIVATE_KEY}
      - AUTH_JWT_PUBLIC_KEY=${JWT_PUBLIC_KEY}
      - CORS_ORIGINS=http://localhost:3000
      - CASBIN_POLICY_SOURCE=database:SELECT role_name, resource_code, action_name FROM permissions
    depends_on:
      - postgres

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=your_app
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## Configuration Validation

### Startup Validation

```java
@Component
public class ConfigurationValidator implements ApplicationRunner {

    @Autowired
    private CasbinProperties casbinProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        validateCasbinConfig();
        validateJwtConfig();
    }

    private void validateCasbinConfig() {
        if (casbinProperties.getPolicySource() == null || 
            casbinProperties.getPolicySource().isBlank()) {
            throw new IllegalStateException("Casbin policy source is required");
        }

        if (casbinProperties.getPolling().isEnabled()) {
            if (casbinProperties.getPolling().getDuration() == null ||
                casbinProperties.getPolling().getDuration().toMinutes() < 1) {
                throw new IllegalStateException("Polling duration must be at least 1 minute");
            }
            
            if (casbinProperties.getPolling().getVersionSource() == null ||
                casbinProperties.getPolling().getVersionSource().isBlank()) {
                throw new IllegalStateException("Version source required when polling enabled");
            }
        }
    }

    private void validateJwtConfig() {
        // Validate JWT keys are loaded properly
        try {
            KeyPair keyPair = applicationContext.getBean(KeyPair.class);
            if (keyPair.getPrivate() == null || keyPair.getPublic() == null) {
                throw new IllegalStateException("JWT keys are not properly configured");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to validate JWT configuration", e);
        }
    }
}
```

## Common Configuration Issues

### Issue 1: Component Scan Missing

**Problem**: Library beans not found

**Solution**:
```java
@ComponentScan(basePackages = {"com.defi.common", "com.yourapp"})
```

### Issue 2: JWT Keys Invalid Format

**Problem**: JWT token generation fails

**Solution**: Verify PEM format và PKCS#8 conversion:
```bash
# Check key format
openssl rsa -in private.pem -text -noout
openssl rsa -pubin -in public.pem -text -noout
```

### Issue 3: CORS Configuration Mismatch

**Problem**: Frontend cannot access API

**Solution**: Library chỉ support origins list:
```yaml
app:
  cors:
    origins:
      - "http://localhost:3000"
```

### Issue 4: Casbin Policies Not Loading

**Problem**: Authorization fails

**Solution**: Check policy source format và database connectivity:
```yaml
app:
  casbin:
    policy-source: "database:SELECT role_name, resource_code, action_name FROM permissions"
```

### Issue 5: Polling Configuration Invalid  

**Problem**: Version polling fails to start

**Solution**: Verify duration và version source:
```yaml
app:
  casbin:
    polling:
      enabled: true
      duration: PT5M  # Minimum PT1M
      version-source: "api:http://service/version"
```

## Best Practices

### 1. Environment Separation
- Use profiles cho different environments
- Store sensitive values trong environment variables
- Validate configuration on startup

### 2. Security
- Never commit RSA keys vào source code
- Use secure key storage trong production
- Rotate keys periodically

### 3. Performance
- Configure appropriate token expiration times
- Use resource filtering cho microservices
- Monitor policy loading performance

### 4. Maintenance  
- Document custom configuration
- Keep library version updated
- Monitor configuration changes

## Required Implementation Checklist

Để sử dụng library, đảm bảo bạn đã:

- [ ] Add library dependency
- [ ] Configure component scan
- [ ] Generate và configure RSA keys
- [ ] Setup required database tables (if using database sources)
- [ ] Create Casbin model file
- [ ] Configure application properties
- [ ] Implement user role management
- [ ] Setup argument resolvers cho web utilities
- [ ] Configure CORS if needed
- [ ] Validate configuration on startup