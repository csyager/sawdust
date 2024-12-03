package com.sawdust.controller.registration.exceptions;

public class InvalidWorkflowTokenException extends RuntimeException {
    public InvalidWorkflowTokenException(String message) {
        super(message);
    }
}
