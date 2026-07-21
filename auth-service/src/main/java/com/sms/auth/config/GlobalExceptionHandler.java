package com.sms.auth.config;

import com.sms.auth.exception.LoginRejectedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import com.sms.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginRejectedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleLoginRejected(
            LoginRejectedException ex, HttpServletRequest request) {
        log.warn("Login rejected at {}: locked={}, attempts={}/{}",
                request.getRequestURI(), ex.isLocked(), ex.getFailedAttempts(), ex.getMaxAttempts());
        Map<String, Object> details = buildErrorDetails(
                ex.isLocked() ? "ACCOUNT_LOCKED" : "INVALID_CREDENTIALS",
                request.getRequestURI(),
                null);
        details.put("locked", ex.isLocked());
        details.put("failedAttempts", ex.getFailedAttempts());
        details.put("maxAttempts", ex.getMaxAttempts());
        details.put("remainingAttempts", ex.getRemainingAttempts());
        details.put("warnLockout", ex.isWarnLockout());
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), details));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed",
                buildErrorDetails("VALIDATION_ERROR", request.getRequestURI(), fieldErrors)));
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBadRequest(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(),
                buildErrorDetails("BAD_REQUEST", request.getRequestURI(), null)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(),
                buildErrorDetails("BAD_REQUEST", request.getRequestURI(), null)));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "A record with this email already exists.";
        if (ex.getMessage() != null && ex.getMessage().contains("registration_invitations")) {
            message = "A registration invitation already exists for this email.";
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(message,
                buildErrorDetails("BAD_REQUEST", request.getRequestURI(), null)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error",
                        buildErrorDetails("INTERNAL_ERROR", request.getRequestURI(), null)));
    }

    private Map<String, Object> buildErrorDetails(String code, String path, Map<String, String> fieldErrors) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("timestamp", Instant.now().toString());
        details.put("code", code);
        details.put("path", path);
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            details.put("fieldErrors", fieldErrors);
        }
        return details;
    }
}
