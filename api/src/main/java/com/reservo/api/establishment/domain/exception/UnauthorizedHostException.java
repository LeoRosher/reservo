package com.reservo.api.establishment.domain.exception;

import java.util.UUID;

public class UnauthorizedHostException extends RuntimeException {
    public UnauthorizedHostException(UUID hostId) {
        super("User with ID " + hostId + " does not have HOST permissions for this action.");
    }

    public UnauthorizedHostException(String message) {
        super(message);
    }

    public UnauthorizedHostException(UUID hostId, UUID establishmentId) {
        super("User with ID " + hostId + " is not the owner of establishment " + establishmentId + ".");
    }
}
