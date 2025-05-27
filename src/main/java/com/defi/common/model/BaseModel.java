package com.defi.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * {@code BaseModel} serves as the base class for all JPA entities in the
 * system.
 * It provides common fields and lifecycle management for database entities.
 *
 * <p>
 * This abstract class includes:
 * </p>
 * <ul>
 * <li>Auto-generated primary key ({@link #id})</li>
 * <li>Creation timestamp ({@link #createdAt})</li>
 * <li>Last update timestamp ({@link #updatedAt})</li>
 * <li>Automatic timestamp management via JPA lifecycle callbacks</li>
 * </ul>
 *
 * <p>
 * All entity classes should extend this base class to inherit common
 * functionality.
 * </p>
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseModel {

    /**
     * Default constructor for JPA and framework usage.
     */

    /**
     * The unique identifier for the entity.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
