package com.defi.common.token.entity;

/**
 * {@code SubjectType} defines the type of entity (subject) that a token can represent.
 * It helps differentiate tokens issued for different system actors, such as users or system agents.
 */
public enum SubjectType {

    /**
     * Token is issued for a system-level actor (e.g., internal service).
     */
    SYSTEM("system"),

    /**
     * Token is issued for a regular authenticated user.
     */
    USER("user"),

    /**
     * Token is issued for an agent (e.g., delegated process or bot).
     */
    AGENT("agent");

    private final String name;

    /**
     * Constructs a {@code SubjectType} with a string identifier.
     *
     * @param name the string name of the subject type
     */
    SubjectType(String name) {
        this.name = name;
    }

    /**
     * Resolves a {@code SubjectType} from a string value (case-insensitive).
     *
     * @param name the subject type name
     * @return the matching {@code SubjectType}, or {@code null} if not found
     */
    public static SubjectType forName(String name) {
        for (SubjectType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Returns the string name of the subject type.
     *
     * @return the subject type name
     */
    public String getName() {
        return name;
    }
}
