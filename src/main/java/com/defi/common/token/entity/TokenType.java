package com.defi.common.token.entity;

/**
 * {@code TokenType} represents the type of token issued by the authentication system.
 * Common types include:
 * <ul>
 *     <li>{@link #ACCESS_TOKEN} – used for authorizing API access</li>
 *     <li>{@link #REFRESH_TOKEN} – used for obtaining new access tokens</li>
 * </ul>
 */
public enum TokenType {

    /**
     * Represents an access token used for authenticating API requests.
     */
    ACCESS_TOKEN("access"),

    /**
     * Represents a refresh token used to obtain a new access token after expiration.
     */
    REFRESH_TOKEN("refresh");

    private final String name;

    /**
     * Constructs a {@code TokenType} with its string name representation.
     *
     * @param name the name of the token type (e.g. "access", "refresh")
     */
    TokenType(String name) {
        this.name = name;
    }

    /**
     * Retrieves a {@code TokenType} enum from its string representation (case-insensitive).
     *
     * @param name the token type name
     * @return the matching {@code TokenType}, or {@code null} if not found
     */
    public static TokenType forName(String name) {
        for (TokenType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Returns the string representation of the token type.
     *
     * @return the token type name
     */
    public String getName() {
        return name;
    }
}
