package com.defi.common.log;

import com.defi.common.log.entity.EventLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A bean for logging structured business or system events.
 * Events are logged as a single JSON line to a dedicated "event" logger,
 * which can be configured to write to a separate file (e.g., logs/event.log).
 */
@Component
@RequiredArgsConstructor
public class EventLogger {

    private static final Logger logger = LoggerFactory.getLogger("event");
    private final ObjectMapper mapper;

    /**
     * Logs an {@link EventLog} object by converting it to a JSON string
     * and writing it to the event logger at the INFO level.
     *
     * @param event The {@link EventLog} to be logged. Must not be null.
     */
    public void log(EventLog event) {
        if (event != null) {
            try {
                logger.info(mapper.writeValueAsString(event));
            } catch (Exception e) {
                logger.error("Failed to serialize EventLog", e);
            }
        }
    }
}
