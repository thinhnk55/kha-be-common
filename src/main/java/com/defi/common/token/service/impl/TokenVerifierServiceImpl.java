package com.defi.common.token.service.impl;

import com.defi.common.token.entity.*;
import com.defi.common.token.service.TokenVerifierService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code TokenVerifierServiceImpl} handles JWT token parsing and validation.
 * <p>
 * This service verifies the signature and expiration of JWT tokens using an RSASSA public key,
 * then extracts token claims and maps them to the application's {@link Token} object.
 * </p>
 *
 * <p>
 * The service requires an {@link RSASSAVerifier} instance for signature verification.
 * </p>
 *
 * @see TokenVerifierService
 */
@Service
@RequiredArgsConstructor
public class TokenVerifierServiceImpl implements TokenVerifierService {

    /**
     * The RSASSA verifier used to verify JWT token signatures.
     */
    private final RSASSAVerifier verifier;

    /**
     * Parses and validates a JWT token string, extracting claims as a {@link Token} object.
     * <p>
     * The method first checks the signature and expiration time; if the token is invalid,
     * {@code null} is returned. Otherwise, claims such as roles, groups, and user information
     * are mapped into a {@link Token} entity.
     * </p>
     *
     * @param token the JWT token string to parse and validate
     * @return a {@link Token} object containing the parsed claims,
     *         or {@code null} if the token is invalid or expired
     * @throws RuntimeException if the token cannot be parsed
     */
    @Override
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
            throw new RuntimeException("Failed to parse JWT token", e);
        }
    }

    /**
     * Validates the provided JWT's signature and expiration.
     * <p>
     * Returns {@code true} only if the token signature is valid and the token has not expired.
     * </p>
     *
     * @param signedJWT the {@link SignedJWT} object to validate
     * @return {@code true} if the token is valid and not expired, {@code false} otherwise
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
}
