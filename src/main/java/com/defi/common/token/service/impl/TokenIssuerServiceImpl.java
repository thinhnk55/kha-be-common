package com.defi.common.token.service.impl;

import com.defi.common.token.entity.ClaimField;
import com.defi.common.token.entity.SubjectType;
import com.defi.common.token.entity.Token;
import com.defi.common.token.entity.TokenType;
import com.defi.common.token.service.TokenIssuerService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * {@code TokenIssuerServiceImpl} is responsible for JWT token generation and refresh.
 * <p>
 * This service uses RSASSASigner to create JWTs signed with RS256.
 * It provides methods for issuing new tokens and refreshing existing ones,
 * embedding user, session, role, and group information as claims.
 * </p>
 *
 * <p>
 * The bean is only created if an {@link RSASSASigner} bean is present in the application context.
 * </p>
 *
 * @see com.defi.common.token.service.TokenIssuerService
 */
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RSASSASigner.class)
public class TokenIssuerServiceImpl implements TokenIssuerService {

    /**
     * The RSASSA signer used to sign JWT tokens.
     */
    private final RSASSASigner signer;

    /**
     * Generates a new JWT token for the specified user and session details.
     *
     * @param sessionId   the session identifier
     * @param type        the type of token (e.g., access, refresh)
     * @param subjectID   the subject (user) identifier
     * @param subjectName the subject (user) name
     * @param roles       the list of roles granted to the user
     * @param groups      the list of groups the user belongs to
     * @param timeToLive  the token's time-to-live in seconds
     * @return a JWT token as a {@link String}
     * @throws RuntimeException if signing fails
     */
    @Override
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
     * Refreshes an existing JWT token, generating a new token with updated issue and expiration times.
     *
     * @param token      the original token to refresh
     * @param timeToLive the new token's time-to-live in seconds
     * @return a refreshed JWT token as a {@link String}
     * @throws RuntimeException if signing fails
     */
    @Override
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
     * <p>
     * The resulting token includes claims such as subject, session ID, roles, and groups,
     * and is cryptographically signed with the provided {@link RSASSASigner}.
     * </p>
     *
     * @param payload the {@link Token} payload to sign
     * @return the serialized JWT string
     * @throws RuntimeException if signing fails
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
                    .claim(ClaimField.SUBJECT_TYPE.getName(), payload.getSubjectType().getName())
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
}
