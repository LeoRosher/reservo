package com.reservo.api.establishment.domain.exception;

import java.util.UUID;

public class EstablishmentNotFoundException extends RuntimeException {
    public EstablishmentNotFoundException(UUID id) {
        super("Establishment not found with ID: " + id);
    }
}
