package com.defi.common.log.entity;

import com.defi.common.token.entity.SubjectType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code EventLog} represents a structured audit or activity log entry in the
 * system.
 * It records the identity and type of the subject, the type of event, the
 * target of the event,
 * its status, timestamp, and any associated metadata in JSON form.
 * 
 * <p>
 * This class uses Lombok annotations to generate constructors:
 * </p>
 * <ul>
 * <li>{@code @NoArgsConstructor} - Creates a default constructor for frameworks
 * and serialization</li>
 * <li>{@code @AllArgsConstructor} - Creates a constructor with all fields as
 * parameters</li>
 * <li>{@code @Builder} - Provides a fluent builder pattern for object
 * creation</li>
 * </ul>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventLog {
    private String id;
    private String targetType;
    private String targetId;
    private SubjectType subjectType;
    private String subjectId;
    private String type;
    private ObjectNode data;
    private String correlationId;
    private Long createdAt;
}
