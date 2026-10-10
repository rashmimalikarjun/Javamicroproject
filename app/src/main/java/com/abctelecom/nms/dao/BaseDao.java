package com.abctelecom.nms.dao;

import com.abctelecom.nms.config.DbConnection;
import com.abctelecom.nms.exception.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Shared JDBC helpers; all queries use PreparedStatement (no string-concatenated SQL). */
public abstract class BaseDao {

    protected Connection open() throws DaoException {
        try {
            return DbConnection.get();
        } catch (SQLException e) {
            throw new DaoException("Database unavailable: " + e.getMessage()
                    + ". Check config.properties / MySQL service.", e);
        }
    }

    protected static void close(AutoCloseable c) {
        if (c != null) try { c.close(); } catch (Exception ignored) {}
    }

    /** Map a ResultSet row to an entity. */
    protected interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }

    protected <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws DaoException {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = open();
            ps = conn.prepareStatement(sql);
            bind(ps, params);
            rs = ps.executeQuery();
            List<T> out = new java.util.ArrayList<>();
            while (rs.next()) out.add(mapper.map(rs));
            return out;
        } catch (SQLException e) {
            throw new DaoException("Query failed: " + e.getMessage(), e);
        } finally {
            close(rs); close(ps); close(conn);
        }
    }

    /** INSERT ... returning generated key. */
    protected int insertReturningKey(String sql, Object... params) throws DaoException {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = open();
            ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            bind(ps, params);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            throw new DaoException("Insert failed: " + e.getMessage(), e);
        } finally {
            close(rs); close(ps); close(conn);
        }
    }

    /** INSERT/UPDATE/DELETE; returns affected rows. Duplicate-key SQLState 23xxx propagates as DaoException. */
    protected int update(String sql, Object... params) throws DaoException {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = open();
            ps = conn.prepareStatement(sql);
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Update failed: " + e.getMessage(), e);
        } finally {
            close(ps); close(conn);
        }
    }

    protected static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p == null) ps.setObject(i + 1, null);
            else if (p instanceof Integer v) ps.setInt(i + 1, v);
            else if (p instanceof Long v) ps.setLong(i + 1, v);
            else if (p instanceof Double v) ps.setDouble(i + 1, v);
            else if (p instanceof Boolean v) ps.setBoolean(i + 1, v);
            else if (p instanceof java.time.LocalDateTime v) ps.setObject(i + 1, v);
            else ps.setString(i + 1, String.valueOf(p));
        }
    }
}
