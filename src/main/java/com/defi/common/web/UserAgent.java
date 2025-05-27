package com.defi.common.web;

import java.lang.annotation.*;

/**
 * {@code UserAgent} is a parameter annotation that automatically injects the
 * client's User-Agent string
 * into controller method parameters.
 *
 * <p>
 * This annotation works with {@link UserAgentArgumentResolver} to extract the
 * User-Agent header
 * from HTTP requests, providing information about the client's browser,
 * operating system, and device.
 * </p>
 *
 * <p>
 * The User-Agent resolution behavior:
 * </p>
 * <ul>
 * <li>Extracts the "User-Agent" header from the HTTP request</li>
 * <li>Returns "unknown" if the header is missing or empty</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {@code @PostMapping("/api/login")}
 * public ResponseEntity&lt;String&gt; login({@code @UserAgent} String userAgent) {
 *     // userAgent contains the client's User-Agent string
 *     log.info("Login attempt from: {}", userAgent);
 *     return ResponseEntity.ok("Login successful");
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserAgent {
}
