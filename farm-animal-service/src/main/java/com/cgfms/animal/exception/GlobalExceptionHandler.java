package com.cgfms.animal.exception;

import com.cgfms.animal.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== APPLICATION EXCEPTIONS ============================

    @ExceptionHandler(DuplicateTagException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleDuplicateTag(DuplicateTagException ex) {
        log.warn("Duplicate tag: {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), extractPath()));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        log.warn("Invalid status transition: {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), extractPath()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), extractPath()));
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), extractPath()));
    }

    // ==================== VALIDATION EXCEPTIONS ============================

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Mono<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Constraint violation");

        log.warn("Constraint violation: {}", details);
        return Mono.just(ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, details, extractPath()));
    }

    /** WebFlux validation exception handler (replaces MethodArgumentNotValidException in MVC). */
    @ExceptionHandler(ServerWebInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleServerWebInput(ServerWebInputException ex) {
        String message = ex.getMessage();
        if (message != null && message.length() > 200) {
            message = message.substring(0, 200);
        }
        log.warn("Bad request: {}", message);
        return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST, message, extractPath()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON: {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Malformed JSON in request body", extractPath()));
    }



    // ==================== FALLBACKS ============================

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected error", ex);
        return Mono.just(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                extractPath()));
    }

    // ==================== HELPER ============================

    private String extractPath() {
        return "unknown";
    }
}
