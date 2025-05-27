package com.defi.common.casbin;

import com.defi.common.api.CommonMessage;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * {@code CasbinAuthorizeAspect} is an AOP aspect that enforces authorization
 * using Casbin RBAC.
 * This aspect intercepts methods annotated with {@link CasbinAuthorize} and
 * performs permission checks.
 *
 * <p>
 * The aspect works by:
 * </p>
 * <ul>
 * <li>Extracting the current user's authentication and roles</li>
 * <li>Using Casbin enforcer to check if any user role has permission for the
 * specified resource and action</li>
 * <li>Throwing appropriate exceptions if authentication fails or access is
 * denied</li>
 * </ul>
 *
 * <p>
 * This provides declarative, method-level authorization control throughout the
 * application.
 * </p>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class CasbinAuthorizeAspect {

    /**
     * Constructor for dependency injection.
     * 
     * @param enforcer the Casbin enforcer for authorization
     */

    /**
     * Casbin enforcer for evaluating authorization policies.
     */
    private final Enforcer enforcer;

    /**
     * Enforces permission checks before method execution for methods annotated with
     * {@link CasbinAuthorize}.
     * Validates that the current authenticated user has the required permissions.
     *
     * @param joinPoint  the method execution join point
     * @param casbinAuth the CasbinAuthorize annotation containing resource and
     *                   action information
     * @throws ResponseStatusException with UNAUTHORIZED status if user is not
     *                                 authenticated
     * @throws ResponseStatusException with FORBIDDEN status if user lacks required
     *                                 permissions
     */
    @Before("@annotation(casbinAuth)")
    public void enforcePermission(JoinPoint joinPoint, CasbinAuthorize casbinAuth) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CommonMessage.UNAUTHORIZED);
        }

        List<String> roles = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String obj = casbinAuth.resource();
        String act = casbinAuth.action();

        boolean permitted = roles.stream()
                .anyMatch(role -> enforcer.enforce(role, obj, act));

        if (!permitted) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, CommonMessage.FORBIDDEN);
        }
    }
}
