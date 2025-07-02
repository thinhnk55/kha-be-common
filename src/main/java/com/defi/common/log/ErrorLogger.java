package com.defi.common.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
/**
 * A structured error logging component that logs exceptions and related context as JSON.
 * This bean replaces the static utility style and uses an injected {@link ObjectMapper}
 * for JSON serialization instead of relying on JsonUtil.
 *
 * <p>It provides a fluent API via {@link ErrorLogBuilder} to construct rich, contextual
 * error logs that are human-readable and machine-parsable.</p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * try {
 *     // Some code that throws
 * } catch (Exception e) {
 *     errorLogger.create("Failed to process", e)
 *                .putContext("userId", 42)
 *                .log();
 * }
 * }</pre>
 */
@RequiredArgsConstructor
public class ErrorLogger {

    private static final Logger logger = LoggerFactory.getLogger("error");
    private final ObjectMapper mapper;

    /**
     * Creates an {@link ErrorLogBuilder} instance with a message and throwable.
     *
     * @param message   the error message
     * @param t         the throwable/exception
     * @return a fluent builder to add context and finalize the log
     */
    public ErrorLogBuilder create(String message, Throwable t) {
        return new ErrorLogBuilder(message, t);
    }

    /**
     * Creates an {@link ErrorLogBuilder} instance with only a throwable.
     *
     * @param t the throwable/exception
     * @return a fluent builder to add context and finalize the log
     */
    public ErrorLogBuilder create(Throwable t) {
        return new ErrorLogBuilder("", t);
    }

    /**
     * Internal method to perform the structured error logging using the provided parameters.
     *
     * @param message the error message
     * @param t       the exception to log
     * @param context additional contextual data as a JSON object
     */
    private void performLog(String message, Throwable t, ObjectNode context) {
        ObjectNode logJson = mapper.createObjectNode();
        logJson.put("timestamp", Instant.now().toString());
        logJson.put("level", "ERROR");
        logJson.put("logger_name", "error");
        logJson.put("thread_name", Thread.currentThread().getName());
        logJson.put("message", message);

        if (t != null) {
            ObjectNode exceptionJson = mapper.createObjectNode();
            exceptionJson.put("class", t.getClass().getName());
            if (t.getMessage() != null) {
                exceptionJson.put("message", t.getMessage());
            }

            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            exceptionJson.put("stack_trace", sw.toString());

            Throwable rootCause = getRootCause(t);
            if (rootCause != t) {
                ObjectNode rootCauseJson = mapper.createObjectNode();
                rootCauseJson.put("class", rootCause.getClass().getName());
                if (rootCause.getMessage() != null) {
                    rootCauseJson.put("message", rootCause.getMessage());
                }
                exceptionJson.set("root_cause", rootCauseJson);
            }

            logJson.set("exception", exceptionJson);
        }

        if (context != null && context.size() > 0) {
            logJson.set("context", context);
        }

        try {
            logger.error(mapper.writeValueAsString(logJson));
        } catch (Exception e) {
            DebugLogger.logger.error("", e);
        }
    }

    /**
     * Extracts the root cause of a throwable.
     *
     * @param throwable the original exception
     * @return the root cause, or the same throwable if no nested cause
     */
    private Throwable getRootCause(Throwable throwable) {
        if (throwable == null) return null;
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }

    /**
     * Fluent builder for structured error logging.
     * Allows chaining of context data before emitting the final structured log.
     */
    public class ErrorLogBuilder {
        private final String message;
        private final Throwable throwable;
        private final ObjectNode context;

        private ErrorLogBuilder(String message, Throwable throwable) {
            this.message = message;
            this.throwable = throwable;
            this.context = mapper.createObjectNode();
        }

        /**
         * Adds structured context via a lambda-based builder.
         *
         * @param contextBuilder lambda that receives the context ObjectNode
         * @return this builder
         */
        public ErrorLogBuilder context(Consumer<ObjectNode> contextBuilder) {
            if (contextBuilder != null) contextBuilder.accept(this.context);
            return this;
        }

        /** Add context value as string */
        public ErrorLogBuilder putContext(String key, String value) {
            context.put(key, value);
            return this;
        }

        /** Add context value as int */
        public ErrorLogBuilder putContext(String key, int value) {
            context.put(key, value);
            return this;
        }

        /** Add context value as long */
        public ErrorLogBuilder putContext(String key, long value) {
            context.put(key, value);
            return this;
        }

        /** Add context value as double */
        public ErrorLogBuilder putContext(String key, double value) {
            context.put(key, value);
            return this;
        }

        /** Add context value as boolean */
        public ErrorLogBuilder putContext(String key, boolean value) {
            context.put(key, value);
            return this;
        }

        /** Add context value as JsonNode */
        public ErrorLogBuilder putContext(String key, JsonNode value) {
            context.set(key, value);
            return this;
        }

        /** Serialize and add arbitrary object as context */
        public ErrorLogBuilder putContext(String key, Object value) {
            context.set(key, mapper.valueToTree(value));
            return this;
        }

        /**
         * Adds multiple key-value pairs from a map to the context.
         *
         * @param map map of context key-values
         * @return this builder
         */
        public ErrorLogBuilder putContext(Map<String, ?> map) {
            if (map != null) {
                map.forEach((key, val) -> context.set(key, mapper.valueToTree(val)));
            }
            return this;
        }

        /**
         * Adds a context value if the condition is true.
         * Useful for lazy/effective logging.
         *
         * @param condition condition to check
         * @param key       context key
         * @param supplier  value supplier
         * @return this builder
         */
        public ErrorLogBuilder putContextIf(boolean condition, String key, Supplier<Object> supplier) {
            if (condition) {
                context.set(key, mapper.valueToTree(supplier.get()));
            }
            return this;
        }

        /**
         * Finalizes the structured log and writes it to the error logger.
         */
        public void log() {
            performLog(this.message, this.throwable, this.context);
        }
    }
}
