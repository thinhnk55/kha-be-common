package com.defi.common.casbin.event;

import com.defi.common.casbin.service.PolicyLoader;
import com.defi.common.casbin.service.VersionPollingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Redis message listener for policy reload events in distributed systems.
 * 
 * <p>
 * This component implements the Spring Data Redis {@link MessageListener}
 * interface
 * to receive policy change notifications via Redis pub/sub mechanism. When a
 * policy
 * reload message is received, it triggers an immediate policy refresh from the
 * database.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Listens for standardized {@link PolicyEventConstant#RELOAD_MESSAGE}
 * events</li>
 * <li>Automatically triggers policy reload via {@link PolicyLoader}</li>
 * <li>Fail-safe operation - errors are logged but don't crash the service</li>
 * <li>Supports distributed policy synchronization across service instances</li>
 * </ul>
 * 
 * <p>
 * Message flow:
 * </p>
 * <ol>
 * <li>Permission updated in database</li>
 * <li>Auth service's PolicyEventPublisher sends reload
 * message to Redis</li>
 * <li>This listener receives the message on configured channel</li>
 * <li>{@link PolicyLoader} reloads policies from database</li>
 * <li>Casbin enforcer is updated with fresh policies</li>
 * </ol>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see PolicyLoader
 * @see PolicyEventConstant
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyEventListener implements MessageListener {

    private final VersionPollingService versionPollingService;
    private final PolicyLoader policyLoader;
    private final Enforcer enforcer;

    /**
     * Handles incoming Redis messages and processes policy reload events.
     * 
     * <p>
     * This method is called by Spring Data Redis when a message is received
     * on the configured channel. It checks if the message is a policy reload
     * request and triggers the appropriate action.
     * </p>
     * 
     * <p>
     * The method is designed to be resilient:
     * </p>
     * <ul>
     * <li>Unknown messages are ignored with debug logging</li>
     * <li>Exceptions are caught and logged without rethrowing</li>
     * <li>Successful reloads are logged for audit purposes</li>
     * </ul>
     * 
     * @param message the Redis message containing the event data (non-null)
     * @param pattern the channel pattern that matched (may be null)
     * @see PolicyEventConstant#RELOAD_MESSAGE
     */
    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            String channel = pattern != null ? new String(pattern) : "unknown";
            String messageBody = new String(message.getBody());

            log.debug("Received message on channel: {} with content: {}",
                    channel, messageBody);

            // Check if it's a reload message
            if (messageBody.startsWith(PolicyEventConstant.RELOAD_MESSAGE)) {
                log.info("Processing policy reload event from channel: {}", channel);
                // Extract version if needed (not used in this implementation)
                long version = Long.parseLong(messageBody.split(":")[1]);
                versionPollingService.setCachedVersion(version);
                policyLoader.loadPolicies(enforcer);

                log.info("Policy reload completed successfully version: {}", version);

            } else {
                log.debug("Ignoring unknown message: {}", messageBody);
            }

        } catch (Exception e) {
            log.error("Failed to process policy reload message, ignoring", e);
        }
    }
}