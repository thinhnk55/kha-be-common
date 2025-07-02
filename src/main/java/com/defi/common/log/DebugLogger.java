package com.defi.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code DebugLogger} is a utility class for accessing named SLF4J loggers.
 * It includes a default "debug" logger for general-purpose, human-readable logging
 * during development and a method to retrieve custom loggers by name.
 * For structured error logging, use the {@link ErrorLogger} class.
 */
public class DebugLogger {

    /**
     * Prevent instantiation of utility class.
     */
    private DebugLogger() {
        // Utility class, not meant to be instantiated
    }

    /**
     * A predefined logger with the name {@code "debug"} for general debugging output.
     * This logger is intended for simple, unstructured, human-readable logs during development.
     */
    public static final Logger logger = LoggerFactory.getLogger("debug");
}
