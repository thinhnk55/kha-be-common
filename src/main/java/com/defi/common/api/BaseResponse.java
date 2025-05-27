package com.defi.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * {@code BaseResponse} represents a standardized API response structure for all
 * endpoints.
 * This record provides a consistent format for returning data, error
 * information, and metadata.
 *
 * <p>
 * The response structure includes:
 * </p>
 * <ul>
 * <li>{@link #code} - HTTP status code or custom response code</li>
 * <li>{@link #message} - Human-readable message or error description</li>
 * <li>{@link #data} - The actual response payload</li>
 * <li>{@link #pagination} - Pagination metadata for list responses</li>
 * <li>{@link #errors} - Detailed error information when applicable</li>
 * </ul>
 *
 * <p>
 * Factory methods are provided for common response scenarios.
 * </p>
 *
 * @param <T>        the type of the response data
 * @param code       HTTP status code or custom response code
 * @param message    Human-readable message describing the response or error
 * @param data       The actual response data payload
 * @param pagination Pagination metadata for paginated responses
 * @param errors     Detailed error information as key-value pairs
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponse<T>(
        /**
         * HTTP status code or custom response code.
         */
        Integer code,

        /**
         * Human-readable message describing the response or error.
         */
        String message,

        /**
         * The actual response data payload.
         */
        T data,

        /**
         * Pagination metadata for paginated responses.
         */
        Pagination pagination,

        /**
         * Detailed error information as key-value pairs.
         */
        Map<String, Object> errors) {
    /**
     * Creates a complete response with all fields specified.
     *
     * @param <T>        the type of the response data
     * @param code       the response code
     * @param message    the response message
     * @param data       the response data
     * @param pagination the pagination metadata
     * @param errors     the error details
     * @return a new {@code BaseResponse} instance
     */
    public static <T> BaseResponse<T> of(int code, String message, T data,
            Pagination pagination, Map<String, Object> errors) {
        return new BaseResponse<>(code, message, data, pagination, errors);
    }

    /**
     * Creates an error response with code, message, and error details.
     *
     * @param <T>     the type of the response data
     * @param code    the error code
     * @param message the error message
     * @param errors  the detailed error information
     * @return a new {@code BaseResponse} instance with error information
     */
    public static <T> BaseResponse<T> of(int code, String message, Map<String, Object> errors) {
        return new BaseResponse<>(code, message, null, null, errors);
    }

    /**
     * Creates a successful response with only data.
     *
     * @param <T>  the type of the response data
     * @param data the response data
     * @return a new {@code BaseResponse} instance with data only
     */
    public static <T> BaseResponse<T> of(T data) {
        return new BaseResponse<>(null, null, data, null, null);
    }

    /**
     * Creates a successful response with data and pagination metadata.
     *
     * @param <T>        the type of the response data
     * @param data       the response data
     * @param pagination the pagination metadata
     * @return a new {@code BaseResponse} instance with data and pagination
     */
    public static <T> BaseResponse<T> of(T data, Pagination pagination) {
        return new BaseResponse<>(null, null, data, pagination, null);
    }

    /**
     * Creates a response with only code and message.
     *
     * @param code    the response code
     * @param message the response message
     * @return a new {@code BaseResponse} instance with code and message
     */
    public static BaseResponse<?> of(int code, String message) {
        return BaseResponse.of(code, message, null, null, null);
    }

    /**
     * Creates a response with code, message, and data.
     *
     * @param <T>     the type of the response data
     * @param code    the response code
     * @param message the response message
     * @param data    the response data
     * @return a new {@code BaseResponse} instance with code, message, and data
     */
    public static <T> BaseResponse<T> of(int code, String message, T data) {
        return BaseResponse.of(code, message, data, null, null);
    }

    /**
     * Creates an error response from an exception.
     * Handles {@link ResponseStatusException} specially to extract status code and
     * message.
     * For other exceptions, returns a generic internal server error response.
     *
     * @param e the exception to convert to a response
     * @return a new {@code BaseResponse} instance representing the error
     */
    public static BaseResponse<?> of(Exception e) {
        if (e instanceof ResponseStatusException ex) {
            return BaseResponse.of(ex.getStatusCode().value(), ex.getBody().getDetail());
        } else {
            return BaseResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "internal_server");
        }
    }
}
