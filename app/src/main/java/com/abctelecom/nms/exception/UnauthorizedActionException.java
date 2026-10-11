package com.abctelecom.nms.exception;

/** Thrown when an action violates role-based access control. Unchecked: programming guard. */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) { super(message); }
}
