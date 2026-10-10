package com.abctelecom.nms.util;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.UnauthorizedActionException;
import com.abctelecom.nms.exception.ValidationException;

import javax.swing.JOptionPane;
import java.sql.SQLException;

/** Centralized, safe error presentation for the Swing UI. Never prints stack traces to the user. */
public final class ErrorHelper {
    private ErrorHelper() {}

    /** Returns a short, user-safe message for any throwable produced by services/DAOs. */
    public static String message(Throwable t) {
        if (t instanceof ValidationException || t instanceof UnauthorizedActionException)
            return t.getMessage();
        if (t instanceof DaoException || hasSqlCause(t))
            return "Database unavailable or query failed.\n" +
                   "Check that MySQL is running and config.properties / QITROM_DB_* variables are correct.\n" +
                   "Details: " + rootMessage(t);
        return "Unexpected error: " + rootMessage(t);
    }

    private static boolean hasSqlCause(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause())
            if (c instanceof SQLException) return true;
        return false;
    }

    private static String rootMessage(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) c = c.getCause();
        return c.getMessage() == null ? c.getClass().getSimpleName() : c.getMessage();
    }

    public static void show(java.awt.Component parent, Throwable t) {
        JOptionPane.showMessageDialog(parent, message(t), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
