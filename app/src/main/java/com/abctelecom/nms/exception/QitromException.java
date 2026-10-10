package com.abctelecom.nms.exception;

/** Base checked exception for QITROM business/data errors. */
public class QitromException extends Exception {
    public QitromException(String message) { super(message); }
    public QitromException(String message, Throwable cause) { super(message, cause); }
}
