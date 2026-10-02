package com.opsflow.opsflow_backend.modules.identity.internal.infrastructure.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.opsflow.opsflow_backend.modules.identity.internal.application.UserAlreadyOnboardedException;

@RestControllerAdvice(assignableTypes = IdentityController.class)
public class IdentityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid fields");

        problem.setTitle("Validation failed");
        problem.setProperty("code", "VALIDATION_ERROR");

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("fieldErrors", fieldErrors);

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleMalformedRequest(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request body could not be read");

        problem.setTitle("Malformed request");
        problem.setProperty("code", "MALFORMED_REQUEST");

        return problem;
    }

    @ExceptionHandler(UserAlreadyOnboardedException.class)
    ProblemDetail handleAlreadyOnboarded(UserAlreadyOnboardedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());

        problem.setTitle("Onboarding conflict");
        problem.setProperty("code", "USER_ALREADY_ONBOARDED");

        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        if (exception instanceof AccessDeniedException accessDeniedException) {
            throw accessDeniedException;
        }

        LOGGER.error("Unhandled identity API exception", exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");

        problem.setTitle("Internal server error");
        problem.setProperty("code", "INTERNAL_ERROR");

        return problem;
    }
}
