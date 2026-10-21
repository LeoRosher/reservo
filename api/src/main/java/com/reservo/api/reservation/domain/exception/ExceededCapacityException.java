package com.reservo.api.reservation.domain.exception;

public class ExceededCapacityException extends RuntimeException {
    public ExceededCapacityException(int guests, int maxCapacity) {
        super("Guest count (" + guests + ") exceeds maximum establishment capacity (" + maxCapacity + ").");
    }
}
