package com.defi.common.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.defi.common.context.ApplicationContextHolder;
import com.defi.common.log.DebugLogger;
import com.defi.common.log.ErrorLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.Map;
/**
 * A custom Logback appender that sends log messages to a Redis Stream
 * using Spring's {@link StringRedisTemplate}.
 *
 * <p>This appender is intended for structured event logging,
 * and can be useful for centralized logging, real-time monitoring,
 * or feeding data pipelines via Redis Streams.</p>
 *
 * <p>To avoid tight coupling with Spring during Logback initialization,
 * it retrieves dependencies like {@link StringRedisTemplate} and {@link ErrorLogger}
 * via a utility {@link ApplicationContextHolder}.</p>
 *
 * <p>Configuration example in <code>logback.xml</code>:</p>
 * <pre>{@code
 * <appender name="REDIS" class="com.defi.common.log.appender.EventRedisAppender">
 *     <streamName>app_event_stream</streamName>
 *     <fieldName>message</fieldName>
 * </appender>
 * }</pre>
 *
 * @see ch.qos.logback.core.AppenderBase
 * @see org.springframework.data.redis.core.StringRedisTemplate
 */
@Slf4j
@Setter
public class EventRedisAppender extends AppenderBase<ILoggingEvent> {

    /**
     * The name of the Redis Stream to publish log messages to.
     */
    private String streamName;

    /**
     * The field name used as the key for the log message in the Redis stream entry.
     */
    private String fieldName;

    /**
     * Redis template will be injected by logback-spring.xml.
     */
    private StringRedisTemplate stringRedisTemplate;


    /**
     * Called for each log event. Converts the message to a simple map
     * and writes it to the configured Redis stream.
     *
     * @param eventObject the logging event to append
     */
    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            String message = eventObject.getFormattedMessage();
            Map<String, String> body = Collections.singletonMap(fieldName, message);
            stringRedisTemplate.opsForStream().add(streamName, body);
        } catch (Exception e) {
            DebugLogger.logger.error("", e);
        }
    }
}
