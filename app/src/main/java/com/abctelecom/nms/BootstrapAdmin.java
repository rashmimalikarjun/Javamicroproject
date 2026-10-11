package com.abctelecom.nms;

import com.abctelecom.nms.dao.UserDao;
import com.abctelecom.nms.model.Role;
import com.abctelecom.nms.model.User;
import com.abctelecom.nms.service.UserService;
import com.abctelecom.nms.util.LogConfig;
import com.abctelecom.nms.util.PasswordUtil;

import java.io.Console;
import java.util.List;

/**
 * Safe bootstrap administrator CLI (development / first-run only).
 * - NEVER embeds or generates a default production password.
 * - Password is read with echo disabled (Console) or stdin, validated by the
 *   same UserService rules as the GUI, hashed with PBKDF2 via PasswordUtil.
 * - Refuses to run if an administrator already exists.
 *
 * Usage: java -cp app/target/qitrom-app.jar:app/target/lib/* com.abctelecom.nms.BootstrapAdmin <username> "<full name>"
 */
public final class BootstrapAdmin {
    public static void main(String[] args) throws Exception {
        LogConfig.init();
        if (args.length < 1) {
            System.err.println("Usage: BootstrapAdmin <username> [\"Full Name\"]");
            System.exit(2);
        }
        UserDao userDao = new UserDao();
        List<User> existing = userDao.findAll();
        for (User u : existing)
            if (u.getRole() == Role.NETWORK_ADMIN && u.isEnabled()) {
                System.err.println("An enabled NETWORK_ADMIN already exists (" + u.getUsername()
                        + "). Bootstrap refused - use that account.");
                System.exit(3);
            }
        String username = args[0];
        String fullName = args.length > 1 ? args[1] : username;

        char[] pwd = readSecret("Administrator password (min 8 chars, letter+digit): ");
        if (pwd == null || pwd.length == 0) {
            System.err.println("No password entered. Aborting (no default password is ever created).");
            System.exit(4);
        }
        String pw = new String(pwd);
        java.util.Arrays.fill(pwd, '\0');

        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setRole(Role.NETWORK_ADMIN);
        // Bootstrap path: no acting principal exists yet; UserService enforces
        // password policy and uniqueness regardless of actor.
        new UserService().createUser(u, pw, null);
        System.out.println("Created NETWORK_ADMIN '" + username + "'. Hash algorithm: "
                + "PBKDF2-HMAC-SHA256" + ". Please log in and create operator accounts via the GUI.");
    }

    private static char[] readSecret(String prompt) throws Exception {
        Console c = System.console();
        if (c != null) return c.readPassword(prompt);
        System.out.print(prompt);
        System.out.flush();
        byte[] buf = System.in.readAllBytes();
        String s = new String(buf).trim();
        return s.isEmpty() ? null : s.toCharArray();
    }
}
