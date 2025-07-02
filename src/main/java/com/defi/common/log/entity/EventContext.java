package com.defi.common.log.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a context for an event, containing the event log and associated data.
 * This class is a generic wrapper that pairs an {@link EventLog} with a context object of any type.
 *
 * @param <T> the type of the context object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventContext<T> {
    /**
     * The event log associated with this context.
     */
    private EventLog event;
    /**
     * The context data associated with the event.
     */
    private T context;
}

