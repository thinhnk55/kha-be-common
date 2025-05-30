package com.defi.common.casbin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a policy rule for Casbin authorization.
 * 
 * <p>
 * This entity encapsulates the core elements of an access control policy rule
 * in the RBAC (Role-Based Access Control) model:
 * </p>
 * <ul>
 * <li><strong>Subject</strong>: The role ID that is being granted access</li>
 * <li><strong>Object</strong>: The resource code that access is being granted
 * to</li>
 * <li><strong>Action</strong>: The action code that specifies what can be
 * done</li>
 * </ul>
 * 
 * <p>
 * Example policy rule: Role ID 1 can perform action "read" on resource "users"
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRule {

    /**
     * Unique identifier of the permission in the database.
     * Used for tracking and auditing purposes.
     */
    private Long id;

    /**
     * Role ID representing the subject in Casbin terms.
     * This identifies which role is being granted the permission.
     */
    private Long roleId;

    /**
     * Resource code representing the object in Casbin terms.
     * This identifies what resource the permission applies to.
     * 
     * @example "users", "roles", "permissions", "reports"
     */
    private String resourceCode;

    /**
     * Action code representing the action in Casbin terms.
     * This specifies what operation can be performed on the resource.
     * 
     * @example "create", "read", "update", "delete"
     */
    private String actionCode;

    /**
     * Converts this policy rule to Casbin policy format.
     * 
     * <p>
     * Casbin expects policies in the format: [subject, object, action]
     * where subject is the role ID, object is the resource, and action is the
     * operation.
     * </p>
     * 
     * @return string array in Casbin format: [roleId, resourceCode, actionCode]
     */
    public String[] toCasbinPolicy() {
        return new String[] {
                String.valueOf(roleId),
                resourceCode,
                actionCode
        };
    }

    /**
     * Returns a human-readable string representation of this policy rule.
     * 
     * @return formatted string showing the policy rule details
     */
    @Override
    public String toString() {
        return String.format("PolicyRule[%d: %d->%s:%s]",
                id, roleId, resourceCode, actionCode);
    }
}