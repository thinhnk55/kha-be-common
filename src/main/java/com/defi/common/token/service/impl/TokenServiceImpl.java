package com.defi.common.token.service.impl;

import com.defi.common.token.entity.ClaimField;
import com.defi.common.token.entity.SubjectType;
import com.defi.common.token.entity.Token;
import com.defi.common.token.entity.TokenType;
import com.defi.common.token.service.TokenService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code TokenServiceImpl} is the default implementation of
 * {@link TokenService} for JWT token management.
 * This service handles JWT token generation, validation, parsing, and
 * refreshing using RSA signatures.
 *
 * <p>
 * The implementation uses the Nimbus JOSE + JWT library for JWT operations and
 * supports:
 * </p>
 * <ul>
 * <li>Token generation with custom claims and RS256 signing</li>
 * <li>Token validation including signature and expiration checks</li>
 * <li>Token parsing and claim extraction</li>
 * <li>Token refresh with updated expiration times</li>
 * </ul>
 *
 * <p>
 * All tokens are signed using RSA-SHA256 (RS256) algorithm for enhanced
 * security.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    /**
     * Constructor for dependency injection.
     * 
     * @param signer   RSA signer for JWT creation
     * @param verifier RSA verifier for JWT validation
     */

    /**
     * RSA signer for creating JWT signatures.
     */
    private final RSASSASigner signer;

    /**
     * RSA verifier for validating JWT signatures.
     */
    private final RSASSAVerifier verifier;

    /**
     * Generates a signed JWT access token with the provided session and subject
     * details.
     *
     * @param sessionId   the unique session identifier
     * @param type        the token type (e.g., access, refresh)
     * @param subjectID   the ID of the subject (usually user ID)
     * @param subjectName the display name or username of the subject
     * @param roles       list of role identifiers granted to the subject
     * @param groups      list of group identifiers the subject belongs to
     * @param timeToLive  token lifetime in seconds
     * @return a signed JWT string
     */
    public String generateToken(String sessionId, TokenType type,
            String subjectID, String subjectName, List<String> roles,
            List<String> groups, long timeToLive) {
        long issuedAt = Instant.now().getEpochSecond();
        Token token = Token.builder()
                .sessionId(sessionId)
                .tokenType(type)
                .subjectId(subjectID)
                .subjectName(subjectName)
                .subjectType(SubjectType.USER)
                .roles(roles)
                .groups(groups)
                .iat(issuedAt)
                .exp(issuedAt + timeToLive)
                .build();
        return signToken(token);
    }

    /**
     * Refreshes an existing token by creating a new one with updated issue and
     * expiration times.
     * The new token maintains all original claims except for timing information.
     *
     * @param token      the original token object to refresh
     * @param timeToLive time-to-live in seconds for the new token
     * @return a new signed JWT string with updated expiration
     */
    public String refreshToken(Token token, int timeToLive) {
        long issuedAt = Instant.now().getEpochSecond();
        Token newToken = Token.builder()
                .tokenType(TokenType.ACCESS_TOKEN)
                .sessionId(token.getSessionId())
                .subjectId(token.getSubjectId())
                .subjectName(token.getSubjectName())
                .subjectType(token.getSubjectType())
                .roles(token.getRoles())
                .groups(token.getGroups())
                .iat(issuedAt)
                .exp(issuedAt + timeToLive)
                .build();
        return signToken(newToken);
    }

    /**
     * Signs a {@link Token} object as a JWT using RS256 algorithm.
     * Creates a JWT with proper headers and claims, then signs it with the RSA
     * private key.
     *
     * @param payload the token payload containing claims to be signed
     * @return a signed JWT string
     * @throws RuntimeException if signing fails due to cryptographic errors
     */
    private String signToken(Token payload) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(payload.getSubjectId())
                    .issueTime(new Date(payload.getIat() * 1000))
                    .expirationTime(new Date(payload.getExp() * 1000))
                    .claim(ClaimField.ID.getName(), payload.getSessionId())
                    .claim(ClaimField.TYPE.getName(), payload.getTokenType().getName())
                    .claim(ClaimField.SUBJECT_NAME.getName(), payload.getSubjectName())
                    .claim(ClaimField.SUBJECT_TYPE.getName(), payload.getSubjectName())
                    .claim(ClaimField.ROLES.getName(), payload.getRoles())
                    .claim(ClaimField.GROUPS.getName(), payload.getGroups())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the signature and expiration of the provided JWT.
     * Performs both cryptographic signature verification and temporal validity
     * checks.
     *
     * @param signedJWT the parsed JWT object to validate
     * @return {@code true} if the token is valid and not expired, otherwise
     *         {@code false}
     */
    public boolean validateToken(SignedJWT signedJWT) {
        try {
            boolean signatureValid = signedJWT.verify(verifier);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            long expiresAt = claims.getExpirationTime().toInstant().getEpochSecond();
            long now = Instant.now().getEpochSecond();
            boolean notExpired = now < expiresAt;
            return signatureValid && notExpired;
        } catch (JOSEException | ParseException e) {
            return false;
        }
    }

    /**
     * Parses a JWT string and returns the {@link Token} object if valid.
     * Validates the token signature and expiration before extracting claims.
     *
     * @param token the JWT string to parse and validate
     * @return a valid {@link Token} object with extracted claims, or {@code null}
     *         if invalid
     * @throws RuntimeException if token parsing fails due to malformed JWT
     *                          structure
     */
    public Token parseToken(String token) {
        try {
            if (token == null) {
                return null;
            }
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!validateToken(signedJWT)) {
                return null;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            List<String> roles = claims.getListClaim(ClaimField.ROLES.getName())
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            List<String> groups = claims.getListClaim(ClaimField.GROUPS.getName())
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            long issuedAt = claims.getIssueTime().toInstant().getEpochSecond();
            long expiresAt = claims.getExpirationTime().toInstant().getEpochSecond();

            return Token.builder()
                    .sessionId((String) claims.getClaim(ClaimField.ID.getName()))
                    .tokenType(TokenType.forName((String) claims.getClaim(ClaimField.TYPE.getName())))
                    .subjectId(claims.getSubject())
                    .subjectName((String) claims.getClaim(ClaimField.SUBJECT_NAME.getName()))
                    .subjectType(SubjectType.forName((String) claims.getClaim(ClaimField.SUBJECT_TYPE.getName())))
                    .roles(roles)
                    .groups(groups)
                    .iat(issuedAt)
                    .exp(expiresAt)
                    .build();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
