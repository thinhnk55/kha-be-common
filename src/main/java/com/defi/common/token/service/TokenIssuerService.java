package com.defi.common.token.service;

import com.defi.common.token.entity.Token;
import com.defi.common.token.entity.TokenType;

import java.util.List;

/**
 * {@code TokenIssuerService} handles JWT token generation and refresh operations.
 * This service provides methods for creating and renewing authentication tokens.
 *
 * <p>
 * The service supports:
 * </p>
 * <ul>
 *   <li>Token generation with custom claims and expiration</li>
 *   <li>Token refresh for extending session lifetime</li>
 * </ul>
 *
 * <p>
 * All tokens are signed using RSA signatures for security.
 * </p>
 */
public interface TokenIssuerService {

    /**
     * Generates a new JWT token with the specified claims and expiration.
     *
     * @param sessionId   unique session identifier
     * @param type        the type of token to generate (access or refresh)
     * @param subjectID   unique identifier of the subject
     * @param subjectName display name of the subject
     * @param roles       list of role identifiers granted to the subject
     * @param groups      list of group identifiers the subject belongs to
     * @param timeToLive  token lifetime in seconds
     * @return the generated JWT token as a string
     */
    String generateToken(String sessionId, TokenType type,
                         String subjectID, String subjectName, List<String> roles,
                         List<String> groups, long timeToLive);

    /**
     * Refreshes an existing token by generating a new one with updated expiration.
     * The new token maintains the same claims as the original token.
     *
     * @param token      the original token to refresh
     * @param timeToLive new token lifetime in seconds
     * @return the refreshed JWT token as a string
     */
    String refreshToken(Token token, int timeToLive);
}
