package com.defi.common.exception;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code GlobalExceptionHandler} provides centralized exception handling for
 * the entire application.
 * This controller advice intercepts exceptions thrown by controllers and
 * converts them into standardized API responses.
 *
 * <p>
 * The handler manages the following exception types:
 * </p>
 * <ul>
 * <li>{@link ResponseStatusException} - HTTP status exceptions with custom
 * messages</li>
 * <li>{@link MethodArgumentNotValidException} - Bean validation failures</li>
 * <li>{@link Exception} - Generic fallback for unhandled exceptions</li>
 * </ul>
 *
 * <p>
 * All responses are formatted using {@link BaseResponse} for consistency across
 * the API.
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles {@link ResponseStatusException} thrown by controllers or services.
     * Extracts the HTTP status code and error message from the exception.
     *
     * @param ex the ResponseStatusException to handle
     * @return a ResponseEntity with standardized error response and appropriate
     *         HTTP status
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<BaseResponse<?>> handleResponseStatusException(ResponseStatusException ex) {
        BaseResponse<?> response = BaseResponse.of(ex.getStatusCode().value(),
                ex.getBody().getDetail());
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    /**
     * Handles validation errors from {@link MethodArgumentNotValidException}.
     * Extracts field-level validation errors and formats them into a detailed error
     * response.
     *
     * @param ex      the MethodArgumentNotValidException containing validation
     *                errors
     * @param request the current web request context
     * @return a ResponseEntity with validation error details and BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex,
            WebRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        BaseResponse<?> response = BaseResponse.of(ex.getStatusCode().value(),
                ex.getBody().getDetail(), errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other unhandled exceptions as a fallback.
     * Provides a generic internal server error response to prevent sensitive
     * information leakage.
     *
     * @param ex      the unhandled exception
     * @param request the current web request context
     * @return a ResponseEntity with generic error message and BAD_REQUEST status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleAllExceptions(Exception ex,
            WebRequest request) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", CommonMessage.INTERNAL_SERVER);
        return new ResponseEntity<>(BaseResponse.of(errors), HttpStatus.BAD_REQUEST);
    }
}
