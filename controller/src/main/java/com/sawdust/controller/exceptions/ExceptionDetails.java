package com.sawdust.controller.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ExceptionDetails {
    private Date timestamp;
    private String message;
}
