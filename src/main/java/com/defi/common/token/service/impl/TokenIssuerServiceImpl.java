package com.defi.common.token.service.impl;

import com.defi.common.token.entity.*;
import com.defi.common.token.service.TokenIssuerService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * {@code TokenIssuerServiceImpl} is responsible for JWT token generation and refresh.
 */
@Service
@RequiredArgsConstructor
public class TokenIssuerServiceImpl implements TokenIssuerService {

    private final RSASSASigner signer;

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
