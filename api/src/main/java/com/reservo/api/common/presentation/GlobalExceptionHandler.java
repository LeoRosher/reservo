package com.reservo.api.common.presentation;

import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.exception.UnauthorizedHostException;
import com.reservo.api.user.domain.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = ErrorResponse.of(
                "USER_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.reservo.api.user.domain.exception.UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(com.reservo.api.user.domain.exception.UserAlreadyExistsException ex) {
        ErrorResponse error = ErrorResponse.of(
                "USER_ALREADY_EXISTS",
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(com.reservo.api.user.domain.exception.InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(com.reservo.api.user.domain.exception.InvalidCredentialsException ex) {
        ErrorResponse error = ErrorResponse.of(
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(EstablishmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEstablishmentNotFound(EstablishmentNotFoundException ex) {
        ErrorResponse error = ErrorResponse.of(
                "ESTABLISHMENT_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedHostException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedHost(UnauthorizedHostException ex) {
        ErrorResponse error = ErrorResponse.of(
                "UNAUTHORIZED_HOST",
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(com.reservo.api.reservation.domain.exception.ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFound(com.reservo.api.reservation.domain.exception.ReservationNotFoundException ex) {
        ErrorResponse error = ErrorResponse.of(
                "RESERVATION_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.reservo.api.reservation.domain.exception.ReservationConflictException.class)
    public ResponseEntity<ErrorResponse> handleReservationConflict(com.reservo.api.reservation.domain.exception.ReservationConflictException ex) {
        ErrorResponse error = ErrorResponse.of(
                "RESERVATION_CONFLICT",
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(com.reservo.api.reservation.domain.exception.InvalidReservationDatesException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReservationDates(com.reservo.api.reservation.domain.exception.InvalidReservationDatesException ex) {
        ErrorResponse error = ErrorResponse.of(
                "INVALID_RESERVATION_DATES",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.reservo.api.reservation.domain.exception.ExceededCapacityException.class)
    public ResponseEntity<ErrorResponse> handleExceededCapacity(com.reservo.api.reservation.domain.exception.ExceededCapacityException ex) {
        ErrorResponse error = ErrorResponse.of(
                "EXCEEDED_CAPACITY",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({org.springframework.dao.PessimisticLockingFailureException.class, org.springframework.dao.CannotAcquireLockException.class})
    public ResponseEntity<ErrorResponse> handleLockingFailure(Exception ex) {
        ErrorResponse error = ErrorResponse.of(
                "CONCURRENCY_CONFLICT",
                "Unable to acquire lock for reservation due to concurrent access. Please retry.",
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse error = ErrorResponse.of(
                "VALIDATION_FAILED",
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(HttpMessageNotReadableException ex) {
        ErrorResponse error = ErrorResponse.of(
                "MALFORMED_REQUEST",
                "Malformed JSON request body",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.of(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = ErrorResponse.of(
                "ILLEGAL_STATE",
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
