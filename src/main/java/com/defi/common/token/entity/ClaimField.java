package com.defi.common.token.entity;

/**
 * {@code ClaimField} defines the standard claim keys used in JWT payloads for access and refresh tokens.
 * These keys are used when building or parsing a {@link com.nimbusds.jwt.JWTClaimsSet}.
 *
 * <p>Each enum constant maps to a short string used in the token's claim body.</p>
 */
public enum ClaimField {

    /**
     * Session ID associated with the token.
     */
    ID("id"),

    /**
     * Token type (e.g., access, refresh).
     */
    TYPE("type"),

    /**
     * Type of the subject (e.g., user, system, agent).
     */
    SUBJECT_TYPE("sub_type"),

    /**
     * Name or username of the subject.
     */
    SUBJECT_NAME("sub_name"),

    /**
     * Roles granted to the subject.
     */
    ROLES("roles"),

    /**
     * Groups associated with the subject.
     */
    GROUPS("group");

    private final String name;

    /**
     * Constructs a {@code ClaimField} with the specified claim key name.
     *
     * @param name the key name used in the JWT payload
     */
    ClaimField(String name) {
        this.name = name;
    }

    /**
     * Resolves a {@code ClaimField} enum from its string name (case-insensitive).
     *
     * @param name the claim field name
     * @return the matching {@code ClaimField}, or {@code null} if not found
     */
    public static ClaimField forName(String name) {
        for (ClaimField claimField : values()) {
            if (claimField.getName().equalsIgnoreCase(name)) {
                return claimField;
            }
        }
        return null;
    }

    /**
     * Returns the string name associated with the claim field.
     *
     * @return the claim field name
     */
    public String getName() {
        return name;
    }
}
