package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.AlertDao;
import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.BaseStationDao;
import com.abctelecom.nms.dao.EdgeServerDao;
import com.abctelecom.nms.dao.TrafficFlowDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.model.Alert;
import com.abctelecom.nms.model.BaseStation;
import com.abctelecom.nms.model.EdgeServer;
import com.abctelecom.nms.model.TrafficFlow;
import com.abctelecom.nms.model.User;

import java.util.List;
import java.util.Map;

/**
 * Threshold-based alert engine. Scans current data against alert_thresholds config.
 * Duplicate OPEN alerts per (type, entity) are prevented by a DB unique key + INSERT IGNORE.
 */
public class AlertService {
    private final AlertDao alerts = new AlertDao();
    private final BaseStationDao stations = new BaseStationDao();
    private final EdgeServerDao edge = new EdgeServerDao();
    private final TrafficFlowDao flows = new TrafficFlowDao();
    private final AuditDao audit = new AuditDao();

    /** Run one evaluation sweep; returns number of NEW alerts created. Off-EDT only. */
    public int evaluateThresholds() throws DaoException {
        Map<String, double[]> th = alerts.thresholds();
        int created = 0;
        double[] cellLoad = th.getOrDefault("CELL_LOAD", new double[]{80, 95});
        for (BaseStation b : stations.findAll()) {
            if (!"ON".equals(b.getPowerStatus())) continue;
            double u = b.utilizationPct();
            if (u >= cellLoad[0]) {
                Alert a = new Alert();
                a.setAlertType("CELL_LOAD");
                a.setEntityId(b.getStationId());
                a.setSeverity(u >= cellLoad[1] ? "CRITICAL" : "WARNING");
                a.setThreshold(u >= cellLoad[1] ? cellLoad[1] : cellLoad[0]);
                a.setActualValue(round2(u));
                a.setMessage(String.format("Cell %s utilization %.1f%% exceeds threshold (%s)",
                        b.getCellName(), u, b.getStationId()));
                if (alerts.insertIfNew(a)) created++;
            }
        }
        double[] latTh = th.getOrDefault("LATENCY", new double[]{50, 100});
        for (TrafficFlow f : flows.findAll()) {
            if (f.getLatencyMs() >= latTh[0]) {
                Alert a = new Alert();
                a.setAlertType("LATENCY");
                a.setEntityId(f.getFlowId());
                a.setSeverity(f.getLatencyMs() >= latTh[1] ? "CRITICAL" : "WARNING");
                a.setThreshold(f.getLatencyMs() >= latTh[1] ? latTh[1] : latTh[0]);
                a.setActualValue(round2(f.getLatencyMs()));
                a.setMessage(String.format("Flow %s latency %.1f ms (%s -> %s)",
                        f.getFlowId(), f.getLatencyMs(), f.getSourceNode(), f.getDestNode()));
                if (alerts.insertIfNew(a)) created++;
            }
        }
        double[] cpuTh = th.getOrDefault("EDGE_CPU", new double[]{75, 90});
        for (EdgeServer e : edge.findAll()) {
            if ("ONLINE".equals(e.getStatus()) && e.getCpuUtilizationPct() >= cpuTh[0]) {
                Alert a = new Alert();
                a.setAlertType("EDGE_CPU");
                a.setEntityId(e.getServerId());
                a.setSeverity(e.getCpuUtilizationPct() >= cpuTh[1] ? "CRITICAL" : "WARNING");
                a.setThreshold(e.getCpuUtilizationPct() >= cpuTh[1] ? cpuTh[1] : cpuTh[0]);
                a.setActualValue(round2(e.getCpuUtilizationPct()));
                a.setMessage(String.format("Edge server %s CPU %.1f%% high at %s",
                        e.getServerId(), e.getCpuUtilizationPct(), e.getSiteLocation()));
                if (alerts.insertIfNew(a)) created++;
            }
        }
        return created;
    }

    public List<Alert> list(int limit) throws DaoException { return alerts.findAll(limit); }

    public void acknowledge(int alertId, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_ALERTS);
        if (alerts.acknowledge(alertId, actor.getUserId()) == 0)
            throw new DaoException("Alert not found or already acknowledged/closed");
        audit.record(actor.getUserId(), "ACK_ALERT", "ALERT", String.valueOf(alertId), null);
    }

    public void close(int alertId, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_ALERTS);
        if (alerts.close(alertId) == 0) throw new DaoException("Alert not found");
        audit.record(actor.getUserId(), "CLOSE_ALERT", "ALERT", String.valueOf(alertId), null);
    }

    private static double round2(double v) { return Math.round(v * 10.0) / 10.0; }
}
