package com.reservo.api.reservation.domain.exception;

public class InvalidReservationDatesException extends RuntimeException {
    public InvalidReservationDatesException(String message) {
        super(message);
    }
}
