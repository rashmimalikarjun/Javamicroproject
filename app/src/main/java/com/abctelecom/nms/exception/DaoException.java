package com.abctelecom.nms.exception;

/** Thrown when a DAO/database operation fails (wraps SQLException etc.). */
public class DaoException extends QitromException {
    public DaoException(String message, Throwable cause) { super(message, cause); }
    public DaoException(String message) { super(message); }
}
