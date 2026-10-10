package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.Role;
import com.abctelecom.nms.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** users table access. Prepared statements only. */
public class UserDao extends BaseDao {

    private static final String COLS =
        "user_id, username, password_hash, full_name, role, enabled, created_at";

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(Role.fromString(rs.getString("role")));
        u.setEnabled(rs.getBoolean("enabled"));
        u.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return u;
    }

    public User findByUsername(String username) throws DaoException {
        List<User> l = query("SELECT " + COLS + " FROM users WHERE username = ?", UserDao::map, username);
        return l.isEmpty() ? null : l.get(0);
    }

    public List<User> findAll() throws DaoException {
        return query("SELECT " + COLS + " FROM users ORDER BY user_id", UserDao::map);
    }

    public int create(User u, String passwordHash) throws DaoException {
        return insertReturningKey(
            "INSERT INTO users (username, password_hash, full_name, role, enabled) VALUES (?,?,?,?,?)",
            u.getUsername(), passwordHash, u.getFullName(), u.getRole().name(), u.isEnabled());
    }

    public int updatePassword(String username, String newHash) throws DaoException {
        return update("UPDATE users SET password_hash = ? WHERE username = ?", newHash, username);
    }

    public int setEnabled(int userId, boolean enabled) throws DaoException {
        return update("UPDATE users SET enabled = ? WHERE user_id = ?", enabled, userId);
    }

    public int count() throws DaoException {
        return query("SELECT COUNT(*) AS c FROM users", rs -> rs.getInt("c")).get(0);
    }
}
