package com.defi.common.config;

import com.defi.common.token.helper.RSAKeyUtil;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;

/**
 * {@code JwtConfig} provides configuration for JWT token generation and
 * validation.
 * This configuration class manages RSA key pairs, token lifetimes, and public
 * paths.
 *
 * <p>
 * Configuration properties are loaded from the {@code auth.jwt} prefix and
 * include:
 * </p>
 * <ul>
 * <li>RSA private and public keys for token signing and verification</li>
 * <li>Token time-to-live settings for access and refresh tokens</li>
 * <li>Public paths that don't require authentication</li>
 * </ul>
 *
 * <p>
 * This class also provides Spring beans for JWT signing and verification
 * operations.
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtConfig {

    /**
     * PEM-formatted RSA private key used for signing JWT tokens.
     */
    private String privateKey;

    /**
     * Passphrase for the RSA private key, if encrypted.
     */
    private String paraphrase;

    /**
     * PEM-formatted RSA public key used for verifying JWT tokens.
     */
    private String publicKey;

    /**
     * Time-to-live duration for access tokens.
     */
    private Duration accessTokenTimeToLive;

    /**
     * Time-to-live duration for refresh tokens.
     */
    private Duration refreshTokenTimeToLive;

    /**
     * List of URL paths that are publicly accessible without authentication.
     */
    private List<String> publicPaths;

    /**
     * Creates an RSA signature verifier bean for JWT token validation.
     * Uses the configured public key to verify token signatures.
     *
     * @return a configured {@link RSASSAVerifier} instance
     * @throws Exception if the public key cannot be loaded or parsed
     */
    @Bean
    public RSASSAVerifier rsassaVerifier() throws Exception {
        RSAPublicKey key = RSAKeyUtil.readRSAPublicKeyFromPEM(publicKey);
        return new RSASSAVerifier(key);
    }

    /**
     * Creates an RSA signature signer bean for JWT token generation.
     * Uses the configured private key and passphrase to sign tokens.
     *
     * @return a configured {@link RSASSASigner} instance
     * @throws Exception if the private key cannot be loaded or parsed
     */
    @Bean
    public RSASSASigner rsassaSigner() throws Exception {
        RSAPrivateKey key = RSAKeyUtil.readRSAPrivateKeyFromPEM(privateKey, paraphrase);
        return new RSASSASigner(key);
    }
}
