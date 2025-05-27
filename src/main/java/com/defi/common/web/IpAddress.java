package com.defi.common.web;

import java.lang.annotation.*;

/**
 * {@code IpAddress} is a parameter annotation that automatically injects the
 * client's IP address
 * into controller method parameters.
 *
 * <p>
 * This annotation works with {@link IpAddressArgumentResolver} to extract the
 * client IP address
 * from HTTP headers, with fallback to the remote address if headers are not
 * available.
 * </p>
 *
 * <p>
 * The IP address resolution follows this priority:
 * </p>
 * <ol>
 * <li>X-Forwarded-For header (for proxy/load balancer scenarios)</li>
 * <li>Remote address from the HTTP request</li>
 * <li>"unknown" if no IP address can be determined</li>
 * </ol>
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {@code @GetMapping("/api/data")}
 * public ResponseEntity&lt;String&gt; getData({@code @IpAddress} String clientIp) {
 *     // clientIp contains the resolved IP address
 *     return ResponseEntity.ok("Request from: " + clientIp);
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IpAddress {
}
