package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.LoginLogDao;
import com.abctelecom.nms.dao.UserDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.User;
import com.abctelecom.nms.util.PasswordUtil;

import java.util.logging.Logger;

/** Authentication & password management. Passwords are verified against PBKDF2 hashes only. */
public class AuthService {
    private static final Logger LOG = java.util.logging.Logger.getLogger(AuthService.class.getName());
    private final UserDao userDao = new UserDao();
    private final LoginLogDao loginLog = new LoginLogDao();
    private User currentUser; // session principal

    public User getCurrentUser() { return currentUser; }

    public synchronized User login(String username, String password) throws QitromException {
        String u = Validators.required(username, "Username");
        Validators.required(password, "Password");
        User found = userDao.findByUsername(u);
        boolean ok = found != null && found.isEnabled() && PasswordUtil.verify(password, found.getPasswordHash());
        if (!ok) {
            loginLog.log(u, "LOGIN_FAILURE");
            LOG.warning("Failed login attempt for '" + u + "'");
            throw new ValidationException("Invalid username or password, or account disabled.");
        }
        loginLog.log(u, "LOGIN_SUCCESS");
        currentUser = found;
        return found;
    }

    public synchronized void logout() throws DaoException {
        if (currentUser != null) {
            loginLog.log(currentUser.getUsername(), "LOGOUT");
            currentUser = null;
        }
    }

    /** Self-service change with old-password verification. */
    public synchronized void changePassword(String oldPwd, String newPwd) throws QitromException {
        if (currentUser == null) throw new ValidationException("Not logged in");
        User db = userDao.findByUsername(currentUser.getUsername());
        if (db == null || !PasswordUtil.verify(oldPwd, db.getPasswordHash()))
            throw new ValidationException("Current password is incorrect");
        validateNew(newPwd);
        userDao.updatePassword(db.getUsername(), PasswordUtil.hash(newPwd));
        loginLog.log(db.getUsername(), "PASSWORD_CHANGE");
    }

    /** Admin-managed reset (no old password needed). */
    public void adminResetPassword(String username, String newPwd) throws QitromException {
        Validators.required(username, "Username");
        validateNew(newPwd);
        User db = userDao.findByUsername(username);
        if (db == null) throw new ValidationException("Unknown user: " + username);
        userDao.updatePassword(username, PasswordUtil.hash(newPwd));
        loginLog.log(username, "PASSWORD_CHANGE");
    }

    private static void validateNew(String newPwd) throws ValidationException {
        if (!PasswordUtil.isStrongEnough(newPwd))
            throw new ValidationException("New password must be at least 8 characters with a letter and a digit");
    }
}
