package com.portfolio.taskmanager.common.exception;

import com.portfolio.taskmanager.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflictException(
            ConflictException ex,
            HttpServletRequest request
    ) {
        log.warn("Conflict error: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        log.warn("Forbidden access attempt: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied on path '{}': {}", request.getRequestURI(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Access denied: You do not possess the required permissions to perform this operation.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failure: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidTaskStatusTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTaskStatusTransitionException(
            InvalidTaskStatusTransitionException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid task status transition: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        log.warn("Bad request: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed for request '{}': {}", request.getRequestURI(), validationErrors);
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Request validation failed. Refer to validationErrors for details.",
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                validationErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        log.warn("Constraint violation: {}", validationErrors);
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Constraint Violation",
                "One or more constraints were violated.",
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleIllegalStateAndArgument(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument or state: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandledException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled internal server exception on URI: {}", request.getRequestURI(), ex);
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected internal server error occurred.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
