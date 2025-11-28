package com.prabhakar.auth.exception;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.prabhakar.auth.dto.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1️⃣ Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.status(400).body(
    		    ApiResponse.error(400, "VALIDATION_ERROR", errors.toString())
    		);
    }

    // 2️⃣ Wrong password OR login failure
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
    	return ResponseEntity.status(401).body(
    		    ApiResponse.error(401, "INVALID_CREDENTIALS", "Username or password is incorrect")
    		);

    }

    // 3️⃣ 403 Forbidden (access denied)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> accessDenied(AccessDeniedException ex) {
//        return ResponseEntity.status(403).body(Map.of(
//                "status", 403,
//                "error", "ACCESS_DENIED",
//                "message", "You do not have permission to access this resource"
//        ));
    	
    	return ResponseEntity.status(403).body(
    			ApiResponse.error(401, "ACCESS_DENIED", "You do not have permission to access this resource"));
    }

    // 4️⃣ Catch-all for any unhandled errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "status", 500,
//                "error", "INTERNAL_SERVER_ERROR",
//                "message", ex.getMessage()
//        ));

    	return ResponseEntity.status(403).body(
    			ApiResponse.error(500, "INTERNAL_SERVER_ERROR", ex.getMessage()));
    }
    
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<?> handleTokenRefreshException(TokenRefreshException ex) {
        // treat reuse/expired refresh tokens as UNAUTHORIZED (401) and tell client to re-login
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "INVALID_REFRESH_TOKEN", ex.getMessage()));
    }
}
