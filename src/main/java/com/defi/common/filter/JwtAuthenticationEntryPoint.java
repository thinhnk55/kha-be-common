package com.defi.common.filter;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * {@code JwtAuthenticationEntryPoint} handles authentication failures in
 * JWT-based security.
 * This entry point is triggered when an unauthenticated user attempts to access
 * a protected resource.
 *
 * <p>
 * The entry point returns a standardized JSON error response with HTTP 401
 * Unauthorized status
 * instead of redirecting to a login page, which is appropriate for REST APIs.
 * </p>
 *
 * <p>
 * The response format follows the {@link BaseResponse} structure for
 * consistency.
 * </p>
 */
@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Constructor for dependency injection.
     * 
     * @param mapper Jackson ObjectMapper for JSON serialization
     */

    /**
     * Jackson ObjectMapper for JSON serialization of error responses.
     */
    private final ObjectMapper mapper;

    /**
     * Handles authentication failures by returning a JSON error response.
     * This method is called whenever an unauthenticated user tries to access a
     * secured resource.
     *
     * @param req           the HTTP request that resulted in authentication failure
     * @param res           the HTTP response to write the error to
     * @param authException the authentication exception that was thrown
     * @throws IOException if writing the response fails
     */
    @Override
    public void commence(HttpServletRequest req,
            HttpServletResponse res,
            AuthenticationException authException) throws IOException {

        res.setContentType("application/json;charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        BaseResponse<?> baseResponse = BaseResponse.of(HttpStatus.UNAUTHORIZED.value(), CommonMessage.UNAUTHORIZED);
        res.getWriter().write(mapper.writeValueAsString(baseResponse));
    }
}