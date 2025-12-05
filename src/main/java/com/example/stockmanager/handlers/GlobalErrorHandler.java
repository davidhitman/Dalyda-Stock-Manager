package com.example.stockmanager.handlers;

import com.example.stockmanager.exceptions.AdminUserExistsException;
import com.example.stockmanager.exceptions.ForbiddenActionException;
import com.example.stockmanager.exceptions.InsufficientStockException;
import com.example.stockmanager.responses.GenericResponse;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.NonNull;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value ={RuntimeException.class, UsernameNotFoundException.class})
    public ResponseEntity<GenericResponse<?>> handleException(Exception exception) {
        if (exception instanceof AccessDeniedException) {
            throw (AccessDeniedException) exception;
        }
        var response = new GenericResponse<>(exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<GenericResponse<?>> handleDuplicateKeyException(DuplicateKeyException exception) {
        var response = new GenericResponse<>(exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse<?>> handleResourceNotFoundException(ResourceNotFoundException exception) {
        var response = new GenericResponse<>(exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GenericResponse<?>> handleBadCredentialsException(BadCredentialsException exception) {
        var response = new GenericResponse<>(exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<GenericResponse<?>> handleForbiddenActionException(ForbiddenActionException exception) {
        var response = new GenericResponse<>(exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {ExpiredJwtException.class})
    public ResponseEntity<GenericResponse<?>> handleExpiredJwtException(ExpiredJwtException exception) {
        GenericResponse<?> response = new GenericResponse<>( exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<GenericResponse<?>> handleIllegalArgumentException(IllegalArgumentException exception) {
        GenericResponse<?> response = new GenericResponse<>(exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InsufficientStockException.class})
    public ResponseEntity<GenericResponse<?>> handleInsufficientStockException(InsufficientStockException exception) {
        GenericResponse<?> response = new GenericResponse<>(exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {AdminUserExistsException.class})
    public ResponseEntity<GenericResponse<?>> handleAdminUserExistsException(AdminUserExistsException exception) {
        GenericResponse<?> response = new GenericResponse<>(exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // handles validations example if email is the right format or if nothing is blank or null
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        GenericResponse<?> response = new GenericResponse<>(errorMessage, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
