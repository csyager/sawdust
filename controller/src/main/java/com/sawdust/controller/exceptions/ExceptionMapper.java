package com.sawdust.controller.exceptions;

import com.sawdust.controller.registration.exceptions.InvalidWorkflowTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class ExceptionMapper {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionDetails> conflictExceptionHandler(ConflictException exception) {
        return new ResponseEntity<>(
                new ExceptionDetails(new Date(), exception.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(InvalidWorkflowTokenException.class)
    public ResponseEntity<ExceptionDetails> invalidWorkflowTokenExceptionHandler(InvalidWorkflowTokenException exception) {
        return new ResponseEntity<>(
                new ExceptionDetails(new Date(), exception.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDetails> catchAllExceptionHandler(Exception exception) {
        return new ResponseEntity<>(
                new ExceptionDetails(new Date(), exception.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
