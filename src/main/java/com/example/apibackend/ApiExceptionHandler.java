package com.example.apibackend;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Consistent error response for frontend:
 * {
 *   timestamp, path, status, error, message, details?
 * }
 *
 * Example:
 * {
 *   "timestamp": "2025-08-18T12:34:56Z",
 *   "path": "/api/admin/payments/42/refund",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Payment not found",
 *   "details": null
 * }
 *
 * Frontend: always check for this shape on error responses.
 */
@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleValidation(Exception ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, req, "Validation failed", ex.getMessage());
        body.put("details", ex instanceof MethodArgumentNotValidException ? ((MethodArgumentNotValidException)ex).getBindingResult().getFieldErrors() : null);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleNotFound(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(baseBody(HttpStatus.NOT_FOUND, req, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler({IllegalStateException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<?> handleConflict(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(baseBody(HttpStatus.CONFLICT, req, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<?> handleForbidden(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(baseBody(HttpStatus.FORBIDDEN, req, "Forbidden", ex.getMessage()));
    }

    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        // Fix: convert HttpStatusCode to HttpStatus
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status.value()).body(baseBody(status, req, ex.getReason(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(baseBody(HttpStatus.INTERNAL_SERVER_ERROR, req, "Internal Server Error", ex.getMessage()));
    }

    private Map<String, Object> baseBody(HttpStatus status, HttpServletRequest req, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("path", req.getRequestURI());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
