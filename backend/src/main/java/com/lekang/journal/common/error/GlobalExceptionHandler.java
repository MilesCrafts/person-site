package com.lekang.journal.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_BASE = "https://journal.lekang.site/problems/";

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "not-found", "Resource not found", exception.getMessage(), request);
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ProblemDetail handleBadRequest(Exception exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", exception.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
        return problem(
            HttpStatus.UNAUTHORIZED,
            "unauthorized",
            "Unauthorized",
            "The username or password is invalid.",
            request
        );
    }

    @ExceptionHandler({ConflictException.class, ObjectOptimisticLockingFailureException.class})
    ProblemDetail handleConflict(Exception exception, HttpServletRequest request) {
        return problem(
            HttpStatus.CONFLICT,
            "conflict",
            "Conflict",
            exception instanceof ConflictException
                ? exception.getMessage()
                : "The resource was changed by another request.",
            request
        );
    }

    @ExceptionHandler(TooManyRequestsException.class)
    ProblemDetail handleTooManyRequests(TooManyRequestsException exception, HttpServletRequest request) {
        return problem(
            HttpStatus.TOO_MANY_REQUESTS,
            "too-many-requests",
            "Too many requests",
            exception.getMessage(),
            request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail handleUploadTooLarge(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return problem(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "payload-too-large",
            "Payload too large",
            "The image must not exceed 8 MB.",
            request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail detail = problem(
            HttpStatus.BAD_REQUEST,
            "validation-error",
            "Validation failed",
            "One or more request fields are invalid.",
            request
        );
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> Map.of(
                "field", error.getField(),
                "message", error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage()
            ))
            .toList();
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("unhandled_request_error path={}", request.getRequestURI(), exception);
        return problem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "internal-error",
            "Internal server error",
            "The request could not be completed.",
            request
        );
    }

    private ProblemDetail problem(
        HttpStatus status,
        String type,
        String title,
        String message,
        HttpServletRequest request
    ) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setType(URI.create(PROBLEM_BASE + type));
        detail.setTitle(title);
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("requestId", MDC.get("requestId"));
        return detail;
    }
}
