package com.defi.common.casbin.event;

/**
 * Constants for policy reload events in Redis messaging system.
 * 
 * <p>This class defines the standard message formats and channel names used
 * for communicating policy changes across distributed services via Redis
 * publish/subscribe mechanism.</p>
 * 
 * <p>All policy-related event communication should use these constants to
 * ensure consistency and compatibility across different services and
 * components in the system.</p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
public class PolicyEventConstant {

    /**
     * Standard reload message that triggers policy reloading.
     * 
     * <p>When this message is received by {@link PolicyEventListener},
     * it will trigger a complete policy reload from the database.</p>
     * 
     * <p>Message format: Simple string without additional parameters</p>
     */
    public static final String RELOAD_MESSAGE = "RELOAD_POLICIES";

    /**
     * Default Redis channel for policy change notifications.
     * 
     * <p>All services should listen on this channel to receive policy
     * reload events. The channel name is designed to be descriptive
     * and avoid conflicts with other Redis usage in the application.</p>
     * 
     * <p>Channel naming convention: {@code casbin:policy:changes}</p>
     */
    public static final String DEFAULT_CHANNEL = "casbin:policy:changes";

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static constants.
     */
    private PolicyEventConstant() {
        // Utility class - no instantiation allowed
    }
}
