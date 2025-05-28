package com.defi.common.filter;

import com.defi.common.config.JwtConfig;
import com.defi.common.token.entity.Token;
import com.defi.common.token.entity.TokenType;
import com.defi.common.token.service.TokenService;
import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * {@code JwtTokenFilter} is a Spring Security filter that processes JWT tokens
 * for authentication.
 * This filter intercepts HTTP requests, validates JWT tokens, and sets up the
 * security context.
 *
 * <p>
 * The filter performs the following operations:
 * </p>
 * <ul>
 * <li>Checks if the request path is public (bypasses authentication)</li>
 * <li>Extracts JWT token from Authorization header</li>
 * <li>Validates and parses the JWT token</li>
 * <li>Creates Spring Security authentication context</li>
 * <li>Returns unauthorized response for invalid tokens</li>
 * </ul>
 *
 * <p>
 * This filter runs once per request and is essential for JWT-based
 * authentication.
 * </p>
 */
@AllArgsConstructor
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    /**
     * Constructor for dependency injection.
     * 
     * @param config       JWT configuration
     * @param tokenService token parsing service
     * @param mapper       JSON object mapper
     */

    /**
     * JWT configuration containing public paths and other settings.
     */
    private final JwtConfig config;

    /**
     * Service for parsing and validating JWT tokens.
     */
    private final TokenService tokenService;

    /**
     * Jackson ObjectMapper for JSON serialization of error responses.
     */
    private final ObjectMapper mapper;

    /**
     * Processes each HTTP request to validate JWT tokens and set up authentication
     * context.
     * Public paths are allowed through without authentication.
     *
     * @param req   the HTTP request
     * @param res   the HTTP response
     * @param chain the filter chain to continue processing
     * @throws ServletException if request processing fails
     * @throws IOException      if I/O operations fail
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
            @NonNull FilterChain chain) throws ServletException, IOException {
        String path = req.getServletPath();

        if (isPublicPath(path)) {
            chain.doFilter(req, res);
            return;
        }

        String accessToken = resolveToken(req);
        Token token = tokenService.parseToken(accessToken);
        if (token == null || token.getTokenType() != TokenType.ACCESS_TOKEN) {
            res.setContentType("application/json;charset=UTF-8");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            BaseResponse<?> baseResponse = BaseResponse.of(HttpStatus.UNAUTHORIZED.value(), CommonMessage.UNAUTHORIZED);
            res.getWriter().write(mapper.writeValueAsString(baseResponse));
            return;
        }
        Authentication auth = getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(req, res);
    }

    /**
     * Checks if the given path is configured as a public path that doesn't require
     * authentication.
     *
     * @param path the request path to check
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        return config.getPublicPaths().stream().anyMatch(path::startsWith);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * Expects the header format: "Bearer {token}"
     *
     * @param req the HTTP request containing the Authorization header
     * @return the JWT token string, or null if not found or invalid format
     */
    private String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }

    /**
     * Creates a Spring Security Authentication object from a validated JWT token.
     * Converts token claims into user principal and granted authorities.
     *
     * @param token the validated JWT token containing user information
     * @return a Spring Security Authentication object
     */
    public Authentication getAuthentication(Token token) {
        var authorities = token.getRoles().stream()
                .map(role -> (GrantedAuthority) () -> role)
                .toList();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                Long.parseLong(token.getSessionId()),
                Long.parseLong(token.getSubjectId()),
                token.getSubjectName(),
                token.getRoles().stream().map(Long::valueOf).toList(),
                token.getGroups().stream().map(Long::valueOf).toList(),
                authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
