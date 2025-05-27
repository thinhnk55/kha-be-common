package com.defi.common.api;

import org.springframework.data.domain.Pageable;

/**
 * {@code Pagination} represents pagination metadata for API responses
 * containing lists of data.
 * This record provides information about the current page, page size, and total
 * number of items.
 *
 * <p>
 * The pagination structure includes:
 * </p>
 * <ul>
 * <li>{@link #page} - Current page number (zero-based)</li>
 * <li>{@link #size} - Number of items per page</li>
 * <li>{@link #total} - Total number of items across all pages</li>
 * </ul>
 *
 * <p>
 * Factory methods are provided for convenient creation from various sources.
 * </p>
 * 
 * @param page  Current page number (zero-based indexing)
 * @param size  Number of items per page
 * @param total Total number of items across all pages
 */
public record Pagination(
        /**
         * Current page number (zero-based indexing).
         */
        int page,

        /**
         * Number of items per page.
         */
        int size,

        /**
         * Total number of items across all pages.
         */
        long total) {
    /**
     * Creates a pagination instance with all fields specified.
     *
     * @param page  the current page number (zero-based)
     * @param size  the number of items per page
     * @param total the total number of items
     * @return a new {@code Pagination} instance
     */
    public static Pagination of(int page, int size, int total) {
        return new Pagination(page, size, total);
    }

    /**
     * Creates a pagination instance with page and size, setting total to 0.
     * Useful when total count is not yet available or not needed.
     *
     * @param page the current page number (zero-based)
     * @param size the number of items per page
     * @return a new {@code Pagination} instance with total set to 0
     */
    public static Pagination of(int page, int size) {
        return new Pagination(page, size, 0);
    }

    /**
     * Creates a pagination instance from a Spring Data {@link Pageable} object.
     * The total count is set to 0 as it's not available in the {@code Pageable}.
     *
     * @param pageable the Spring Data pageable object
     * @return a new {@code Pagination} instance based on the pageable
     */
    public static Pagination of(Pageable pageable) {
        return Pagination.of(
                pageable.getPageNumber(),
                pageable.getPageSize());
    }
}
