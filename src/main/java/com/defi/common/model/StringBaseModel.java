package com.defi.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * {@code StringBaseModel} serves as the base class for JPA entities that use
 * String primary keys.
 * This abstract class provides common fields and lifecycle management for
 * database entities with string IDs.
 *
 * <p>
 * This abstract class includes:
 * </p>
 * <ul>
 * <li>String primary key ({@link #id})</li>
 * <li>Creation timestamp ({@link #createdAt})</li>
 * <li>Last update timestamp ({@link #updatedAt})</li>
 * <li>Automatic timestamp management via JPA lifecycle callbacks</li>
 * </ul>
 *
 * <p>
 * Use this base class for entities that require string-based identifiers
 * instead of auto-generated numeric IDs.
 * </p>
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class StringBaseModel {

    /**
     * Default constructor for JPA and framework usage.
     */

    /**
     * The unique string identifier for the entity.
     */
    @Id
    private String id;

    /**
     * Timestamp when the entity was first created.
     * Set automatically during entity persistence and cannot be updated.
     */
    @Column(updatable = false)
    private Long createdAt;

    /**
     * Timestamp when the entity was last updated.
     * Updated automatically on every entity modification.
     */
    private Long updatedAt;

    /**
     * JPA lifecycle callback executed before entity persistence.
     * Sets both {@link #createdAt} and {@link #updatedAt} to current timestamp.
     */
    @PrePersist
    public void prePersist() {
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * JPA lifecycle callback executed before entity update.
     * Updates the {@link #updatedAt} timestamp to current time.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}
