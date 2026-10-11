package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.Alert;

import java.sql.ResultSet;
import java.util.List;

public class AlertDao extends BaseDao {

    private static Alert map(ResultSet rs) throws java.sql.SQLException {
        Alert a = new Alert();
        a.setAlertId(rs.getInt("alert_id"));
        a.setAlertType(rs.getString("alert_type"));
        a.setSeverity(rs.getString("severity"));
        a.setEntityId(rs.getString("entity_id"));
        a.setMessage(rs.getString("message"));
        double t = rs.getDouble("threshold"); a.setThreshold(rs.wasNull() ? null : t);
        double v = rs.getDouble("actual_value"); a.setActualValue(rs.wasNull() ? null : v);
        a.setStatus(rs.getString("status"));
        if (rs.getTimestamp("created_at") != null) a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        int ack = rs.getInt("acknowledged_by"); a.setAcknowledgedBy(rs.wasNull() ? null : ack);
        return a;
    }

    public List<Alert> findAll(int limit) throws DaoException {
        return query("SELECT * FROM alerts ORDER BY created_at DESC, alert_id DESC LIMIT ?",
                AlertDao::map, Math.max(1, Math.min(500, limit)));
    }

    /**
     * Insert an OPEN alert; duplicates for the same (type, entity) with status OPEN
     * are silently skipped via INSERT IGNORE against the unique key uq_alert_open.
     * Returns true when a new row was created.
     */
    public boolean insertIfNew(Alert a) throws DaoException {
        int n = update("INSERT IGNORE INTO alerts (alert_type, severity, entity_id, message, threshold, actual_value, status)" +
                       " VALUES (?,?,?,?,?,?, 'OPEN')",
                       a.getAlertType(), a.getSeverity(), a.getEntityId(), a.getMessage(),
                       a.getThreshold(), a.getActualValue());
        return n > 0;
    }

    public int acknowledge(int alertId, int userId) throws DaoException {
        return update("UPDATE alerts SET status='ACKNOWLEDGED', acknowledged_by=? WHERE alert_id=? AND status='OPEN'",
                      userId, alertId);
    }

    public int close(int alertId) throws DaoException {
        return update("UPDATE alerts SET status='CLOSED' WHERE alert_id=?", alertId);
    }

    /** Threshold config rows as [warn, crit] keyed by alert type. */
    public java.util.Map<String, double[]> thresholds() throws DaoException {
        java.util.Map<String, double[]> m = new java.util.HashMap<>();
        query("SELECT alert_type, warn_value, crit_value FROM alert_thresholds WHERE enabled=1", rs -> {
            m.put(rs.getString("alert_type"),
                  new double[]{ rs.getDouble("warn_value"), rs.getDouble("crit_value") });
            return m;
        });
        return m;
    }
}
