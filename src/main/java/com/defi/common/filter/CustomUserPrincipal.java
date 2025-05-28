package com.defi.common.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/**
 * {@code CustomUserPrincipal} represents an authenticated user in the Spring
 * Security context.
 * This implementation of {@link UserDetails} holds user information extracted
 * from JWT tokens.
 *
 * <p>
 * The principal contains:
 * </p>
 * <ul>
 * <li>User identification ({@link #userId}, {@link #username})</li>
 * <li>Authorization information ({@link #roles}, {@link #groups})</li>
 * <li>Spring Security authorities ({@link #authorities})</li>
 * </ul>
 *
 * <p>
 * This class is typically created during JWT token validation and used
 * throughout
 * the request lifecycle for authorization decisions.
 * </p>
 */
@AllArgsConstructor
@Getter
public class CustomUserPrincipal implements UserDetails {
    /**
     * The unique identifier of the authenticated session.
     */
    private final Long sessionId;

    /**
     * The unique identifier of the authenticated user.
     */
    private final Long userId;

    /**
     * The username or display name of the authenticated user.
     */
    private final String username;

    /**
     * List of role IDs assigned to the user.
     */
    private final List<Long> roles;

    /**
     * List of group IDs the user belongs to.
     */
    private final List<Long> groups;

    /**
     * Spring Security authorities granted to the user.
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Returns the authorities granted to the user.
     *
     * @return a collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the password used to authenticate the user.
     * Always returns {@code null} as JWT authentication doesn't use passwords.
     *
     * @return {@code null} for JWT-based authentication
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return username;
    }
}
