package com.defi.common.casbin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code CasbinAuthorize} is an annotation for method-level authorization using
 * Casbin RBAC.
 * This annotation enables declarative access control by specifying the required
 * resource and action.
 *
 * <p>
 * The annotation works with {@link CasbinAuthorizeAspect} to enforce
 * authorization rules
 * before method execution. It uses Casbin's policy engine to determine if the
 * current user
 * has permission to perform the specified action on the given resource.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {@code @CasbinAuthorize(resource = "user", action = "read")}
 * public List&lt;User&gt; getAllUsers() {
 *     // Method implementation
 * }
 * </pre>
 *
 * <p>
 * The annotation can be applied to both classes and methods, with method-level
 * annotations
 * taking precedence over class-level ones.
 * </p>
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CasbinAuthorize {

    /**
     * The resource name for authorization check.
     * This should match the resource definitions in your Casbin policy.
     *
     * @return the resource name
     */
    String resource();

    /**
     * The action to be performed on the resource.
     * Common actions include "read", "write", "delete", etc.
     *
     * @return the action name
     */
    String action();
}