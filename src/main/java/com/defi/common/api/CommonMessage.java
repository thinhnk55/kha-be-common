package com.defi.common.api;

/**
 * {@code CommonMessage} provides standardized message constants used throughout
 * the API responses.
 * These constants ensure consistent error and status messages across all
 * endpoints.
 *
 * <p>
 * This utility class contains predefined message keys that can be used for:
 * </p>
 * <ul>
 * <li>Validation error messages</li>
 * <li>HTTP status descriptions</li>
 * <li>Business logic error indicators</li>
 * <li>Success/failure notifications</li>
 * </ul>
 *
 * <p>
 * All constants are immutable and follow lowercase underscore naming
 * convention.
 * </p>
 */
public final class CommonMessage {

    /**
     * Message indicating invalid input or data format.
     */
    public static final String INVALID = "invalid";

    /**
     * Message indicating a resource already exists.
     */
    public static final String EXISTING = "existing";

    /**
     * Message indicating a required field or parameter is missing.
     */
    public static final String REQUIRED = "required";

    /**
     * Message indicating a requested resource was not found.
     */
    public static final String NOT_FOUND = "not_found";

    /**
     * Message indicating authentication is required or failed.
     */
    public static final String UNAUTHORIZED = "unauthorized";

    /**
     * Message indicating access to the resource is forbidden.
     */
    public static final String FORBIDDEN = "forbidden";

    /**
     * Message indicating a conflict with the current state of the resource.
     */
    public static final String CONFLICT = "conflict";

    /**
     * Message indicating successful operation completion.
     */
    public static final String SUCCESS = "success";

    /**
     * Message indicating operation failure.
     */
    public static final String FAIL = "fail";

    /**
     * Message indicating an internal server error occurred.
     */
    public static final String INTERNAL_SERVER = "internal_server";

    /**
     * Message indicating a limit has been exceeded.
     */
    public static final String LIMIT = "limit";

    /**
     * Message indicating a resource is locked or temporarily unavailable.
     */
    public static final String LOCKED = "locked";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CommonMessage() {
    }
}
