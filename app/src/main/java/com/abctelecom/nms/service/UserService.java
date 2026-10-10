package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.UserDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.Role;
import com.abctelecom.nms.model.User;
import com.abctelecom.nms.util.PasswordUtil;

import java.util.List;

/** User administration (NETWORK_ADMIN only). */
public class UserService {
    private final UserDao dao = new UserDao();
    private final AuditDao audit = new AuditDao();

    public List<User> list() throws DaoException { return dao.findAll(); }

    public int createUser(User in, String plainPassword, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_USERS);
        in.setUsername(Validators.id(in.getUsername(), "Username"));
        in.setFullName(Validators.required(in.getFullName(), "Full name"));
        if (in.getRole() == null) throw new ValidationException("Role is required");
        if (!PasswordUtil.isStrongEnough(plainPassword))
            throw new ValidationException("Password must be >= 8 chars with letter and digit");
        if (dao.findByUsername(in.getUsername()) != null)
            throw new ValidationException("Username already exists: " + in.getUsername());
        int id = dao.create(in, PasswordUtil.hash(plainPassword));
        audit.record(actor.getUserId(), "CREATE_USER", "USER", in.getUsername(), "role=" + in.getRole());
        return id;
    }

    public void setEnabled(int userId, boolean enabled, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_USERS);
        if (actor.getUserId() == userId && !enabled)
            throw new ValidationException("You cannot disable your own account");
        if (dao.setEnabled(userId, enabled) == 0) throw new ValidationException("Unknown user id: " + userId);
        audit.record(actor.getUserId(), enabled ? "ENABLE_USER" : "DISABLE_USER", "USER",
                     String.valueOf(userId), null);
    }

    public void resetPassword(String username, String newPwd, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_USERS);
        AuthService svc = new AuthService();
        // adminResetPassword does its own strength validation
        svc.adminResetPassword(username, newPwd);
        audit.record(actor.getUserId(), "RESET_PASSWORD", "USER", username, "hash updated; no password logged");
    }

    public List<User> usersWithRole(Role role) throws DaoException {
        return dao.findAll().stream().filter(u -> u.getRole() == role).toList();
    }
}
