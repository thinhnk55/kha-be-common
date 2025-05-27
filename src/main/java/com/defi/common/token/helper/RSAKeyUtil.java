package com.defi.common.token.helper;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * {@code RSAKeyUtil} is a utility class for generating, serializing, and parsing RSA key pairs
 * using the Bouncy Castle cryptography provider.
 *
 * <p>Supports operations such as:</p>
 * <ul>
 *     <li>Generating 2048-bit RSA key pairs</li>
 *     <li>Exporting public/private keys to PEM format</li>
 *     <li>Reading encrypted or plain PEM-formatted keys</li>
 *     <li>Converting {@link RSAPublicKey} and {@link RSAPrivateKey} to PEM strings</li>
 * </ul>
 */
public class RSAKeyUtil {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private RSAKeyUtil() {
        // Utility class – no instances allowed
    }

    /**
     * Generates a 2048-bit RSA key pair.
     *
     * @return the generated {@link KeyPair}
     * @throws Exception if key generation fails
     */
    public static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    /**
     * Saves the given RSA key pair to PEM-formatted files with optional encryption for the private key.
     *
     * @param publicKeyFilePath  path to save the public key PEM file
     * @param privateKeyFilePath path to save the private key PEM file
     * @param keyPair            the RSA key pair to save
     * @param passphrase         optional passphrase for encrypting the private key
     * @throws Exception if file writing or PEM encoding fails
     */
    public static void saveKeyPairToPEMFiles(String publicKeyFilePath, String privateKeyFilePath,
                                             KeyPair keyPair, String passphrase) throws Exception {
        // Write public key
        try (PemWriter pemWriter = new PemWriter(
                new OutputStreamWriter(new FileOutputStream(publicKeyFilePath), StandardCharsets.UTF_8))) {
            PemObject pemObject = new PemObject("PUBLIC KEY", keyPair.getPublic().getEncoded());
            pemWriter.writeObject(pemObject);
        }

        // Write private key (optionally encrypted)
        try (PemWriter pemWriter = new PemWriter(
                new OutputStreamWriter(new FileOutputStream(privateKeyFilePath), StandardCharsets.UTF_8))) {
            PemObject pemObject;
            if (passphrase != null && !passphrase.isEmpty()) {
                OutputEncryptor encryptor = (OutputEncryptor) new JcePEMEncryptorBuilder("AES-256-CBC")
                        .build(passphrase.toCharArray());
                JcaPKCS8Generator pkcs8Gen = new JcaPKCS8Generator(keyPair.getPrivate(), encryptor);
                pemObject = pkcs8Gen.generate();
            } else {
                PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(keyPair.getPrivate().getEncoded());
                pemObject = new PemObject("PRIVATE KEY", pkInfo.getEncoded());
            }
            pemWriter.writeObject(pemObject);
        }
    }

    /**
     * Parses a PEM-encoded public key string into an {@link RSAPublicKey}.
     *
     * @param pemString the PEM-formatted public key string
     * @return the parsed {@link RSAPublicKey}
     * @throws Exception if parsing fails or the format is invalid
     */
    public static RSAPublicKey readRSAPublicKeyFromPEM(String pemString) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemString))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            if (object instanceof PEMKeyPair keyPair) {
                return (RSAPublicKey) converter.getKeyPair(keyPair).getPublic();
            }
            if (object instanceof org.bouncycastle.asn1.x509.SubjectPublicKeyInfo keyInfo) {
                return (RSAPublicKey) converter.getPublicKey(keyInfo);
            }
            throw new IllegalArgumentException("Invalid public key format");
        }
    }

    /**
     * Parses a PEM-encoded private key string into an {@link RSAPrivateKey}, with optional decryption.
     *
     * @param pemString  the PEM-formatted private key string
     * @param passphrase the passphrase (if the key is encrypted)
     * @return the parsed {@link RSAPrivateKey}
     * @throws Exception if parsing or decryption fails
     */
    public static RSAPrivateKey readRSAPrivateKeyFromPEM(String pemString, String passphrase) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemString))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (object instanceof PEMEncryptedKeyPair encryptedKeyPair) {
                if (passphrase == null || passphrase.isEmpty()) {
                    throw new IllegalArgumentException("Passphrase is required for encrypted key");
                }
                PEMKeyPair keyPair = encryptedKeyPair.decryptKeyPair(
                        new JcePEMDecryptorProviderBuilder().build(passphrase.toCharArray()));
                return (RSAPrivateKey) converter.getKeyPair(keyPair).getPrivate();
            } else if (object instanceof PEMKeyPair keyPair) {
                return (RSAPrivateKey) converter.getKeyPair(keyPair).getPrivate();
            } else if (object instanceof PrivateKeyInfo pkInfo) {
                return (RSAPrivateKey) converter.getPrivateKey(pkInfo);
            } else {
                throw new IllegalArgumentException("Invalid private key format");
            }
        }
    }

    /**
     * Converts an {@link RSAPublicKey} into a PEM-formatted string.
     *
     * @param publicKey the public key
     * @return the PEM string
     */
    public static String getPublicKeyPEM(RSAPublicKey publicKey) {
        return convertToPEM(publicKey, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
    }

    /**
     * Converts an {@link RSAPrivateKey} into a PEM-formatted string.
     *
     * @param privateKey the private key
     * @return the PEM string
     */
    public static String getPrivateKeyPEM(RSAPrivateKey privateKey) {
        return convertToPEM(privateKey, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
    }

    /**
     * Converts a raw {@link Key} to a PEM-formatted string with given start/end markers.
     *
     * @param key         the key to encode
     * @param beginMarker PEM header line (e.g., {@code -----BEGIN PUBLIC KEY-----})
     * @param endMarker   PEM footer line (e.g., {@code -----END PUBLIC KEY-----})
     * @return the formatted PEM string
     */
    private static String convertToPEM(Key key, String beginMarker, String endMarker) {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                .encodeToString(key.getEncoded());
        return beginMarker + "\n" + encoded + "\n" + endMarker;
    }
}
