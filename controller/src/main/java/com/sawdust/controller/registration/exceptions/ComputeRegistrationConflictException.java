package com.sawdust.controller.registration.exceptions;

import com.sawdust.controller.exceptions.ConflictException;

public class ComputeRegistrationConflictException extends ConflictException {
    public ComputeRegistrationConflictException(String computeId) {
        super(String.format("Compute resource with computeId %s already exists.", computeId));
    }
}
