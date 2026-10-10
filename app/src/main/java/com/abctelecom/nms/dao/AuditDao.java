package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** audit_log writer/reader. Details must be sanitized (no secrets). */
public class AuditDao extends BaseDao {

    public void record(Integer userId, String action, String entityType, String entityId, String details)
            throws DaoException {
        update("INSERT INTO audit_log (user_id, action, entity_type, entity_id, details) VALUES (?,?,?,?,?)",
               userId, action, entityType, entityId, details);
    }

    public List<Map<String, Object>> recent(int limit) throws DaoException {
        // whitelist limit to avoid parameter quirks; value is app-controlled, not user text
        int lim = Math.max(1, Math.min(500, limit));
        return query("SELECT a.audit_id, u.username, a.action, a.entity_type, a.entity_id, a.details, a.performed_at " +
                     "FROM audit_log a LEFT JOIN users u ON u.user_id = a.user_id " +
                     "ORDER BY a.performed_at DESC LIMIT ?",
                     rs -> Map.of(
                        "id", (Object) rs.getInt("audit_id"),
                        "user", (Object) String.valueOf(rs.getString("username")),
                        "action", (Object) rs.getString("action"),
                        "entityType", (Object) rs.getString("entity_type"),
                        "entityId", (Object) String.valueOf(rs.getString("entity_id")),
                        "details", (Object) String.valueOf(rs.getString("details")),
                        "at", (Object) (rs.getTimestamp("performed_at") == null ? LocalDateTime.now()
                                    : rs.getTimestamp("performed_at").toLocalDateTime())), lim);
    }
}
