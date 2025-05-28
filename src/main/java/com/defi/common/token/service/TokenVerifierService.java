package com.defi.common.token.service;

import com.defi.common.token.entity.Token;

/**
 * {@code TokenVerifierService} handles JWT token parsing and validation.
 * This service provides methods for extracting and verifying claims from tokens.
 *
 * <p>
 * All tokens are verified using RSA signatures for security.
 * </p>
 */
public interface TokenVerifierService {

    /**
     * Parses and validates a JWT token string, extracting its claims.
     *
     * @param token the JWT token string to parse
     * @return a {@link Token} object containing the parsed claims
     * @throws RuntimeException if the token is invalid or expired
     */
    Token parseToken(String token);
}
