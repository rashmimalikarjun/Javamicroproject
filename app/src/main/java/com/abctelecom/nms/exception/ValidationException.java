package com.abctelecom.nms.exception;

/** Thrown when user-supplied input fails validation. */
public class ValidationException extends QitromException {
    public ValidationException(String message) { super(message); }
}
