package com.defi.common.token.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {@code Token} represents the full payload of a JWT issued by the
 * authentication system.
 * It includes session, subject, access roles, token type, and expiration
 * metadata.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    /**
     * Default constructor for framework usage.
     */

    /**
     * Unique session ID associated with the token.
     */
    private String sessionId;

    /**
     * ID of the subject the token represents (e.g., UUID).
     */
    private String subjectId;

    /**
     * Display name or username of the subject.
     */
    private String subjectName;

    /**
     * Type of the subject (user, system, agent).
     */
    private SubjectType subjectType;

    /**
     * List of role IDs granted to the subject.
     */
    private List<String> roles;

    /**
     * List of group IDs the subject belongs to.
     */
    private List<String> groups;

    /**
     * Issued-at time in seconds since epoch.
     */
    private long iat;

    /**
     * Expiration time in seconds since epoch.
     */
    private long exp;

    /**
     * Type of token (access or refresh).
     */
    private TokenType tokenType;
}
