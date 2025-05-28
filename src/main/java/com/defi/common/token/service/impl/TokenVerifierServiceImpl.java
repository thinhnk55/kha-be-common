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
 * {@code TokenVerifierServiceImpl} is responsible for JWT token validation and parsing.
 */
@Service
@RequiredArgsConstructor
public class TokenVerifierServiceImpl implements TokenVerifierService {

    private final RSASSAVerifier verifier;

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
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the signature and expiration of the provided JWT.
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
