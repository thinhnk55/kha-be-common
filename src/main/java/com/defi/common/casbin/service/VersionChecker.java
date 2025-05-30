package com.defi.common.casbin.service;

import java.util.Optional;

/**
 * Interface for checking version numbers from different sources.
 * 
 * <p>
 * This interface abstracts version checking logic to support
 * different sources like database direct access or API endpoints.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
public interface VersionChecker {

    /**
     * Gets the current version for a specific component code.
     * 
     * @param code the component code (e.g., "policy_version")
     * @return current version number, or empty if not found or error occurred
     */
    Optional<Long> getCurrentVersion(String code);

    /**
     * Checks if the version checker is available and working.
     * 
     * @return true if version checking is operational
     */
    boolean isAvailable();

    /**
     * Gets a description of this version checker implementation.
     * 
     * @return description string
     */
    String getDescription();
}