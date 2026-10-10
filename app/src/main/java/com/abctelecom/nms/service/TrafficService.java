package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.TrafficFlowDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.TrafficFlow;
import com.abctelecom.nms.model.User;

import java.util.List;

/** Traffic monitoring service. */
public class TrafficService {
    private final TrafficFlowDao dao = new TrafficFlowDao();
    private final AuditDao audit = new AuditDao();

    public List<TrafficFlow> list() throws DaoException { return dao.findAll(); }

    public static TrafficFlow normalize(TrafficFlow in) throws ValidationException {
        TrafficFlow f = new TrafficFlow();
        f.setFlowId(Validators.id(in.getFlowId(), "Flow ID"));
        f.setSourceNode(Validators.required(in.getSourceNode(), "Source node"));
        f.setDestNode(Validators.required(in.getDestNode(), "Destination node"));
        if (f.getSourceNode().equals(f.getDestNode()))
            throw new ValidationException("Source and destination nodes must differ");
        f.setBandwidthMbps(Validators.rangeDouble(String.valueOf(in.getBandwidthMbps()), "Bandwidth (Mbps)", 0.01, 1_000_000));
        f.setLatencyMs(Validators.rangeDouble(String.valueOf(in.getLatencyMs()), "Latency (ms)", 0, 10_000));
        f.setPacketLossPct(Validators.rangeDouble(String.valueOf(in.getPacketLossPct()), "Packet loss (%)", 0, 100));
        f.setThroughputMbps(Validators.rangeDouble(String.valueOf(in.getThroughputMbps()), "Throughput (Mbps)", 0, 1_000_000));
        if (f.getThroughputMbps() > f.getBandwidthMbps())
            throw new ValidationException("Throughput cannot exceed allocated bandwidth");
        f.setCongestionStatus(deriveCongestion(in.getCongestionStatus(), f.getLatencyMs(), f.getPacketLossPct()));
        return f;
    }

    /** Deterministic congestion classification unless explicitly provided. */
    static String deriveCongestion(String provided, double latencyMs, double lossPct) throws ValidationException {
        if (provided != null && !provided.isBlank())
            return Validators.oneOf(provided, "Congestion status", Validators.CONGESTION);
        if (latencyMs >= 50 || lossPct >= 2.0) return "CRITICAL";
        if (latencyMs >= 20 || lossPct >= 0.5) return "HIGH";
        return "NORMAL";
    }

    public void create(TrafficFlow in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MONITOR_TRAFFIC);
        TrafficFlow f = normalize(in);
        if (list().stream().anyMatch(x -> x.getFlowId().equals(f.getFlowId())))
            throw new ValidationException("Flow ID already exists: " + f.getFlowId());
        dao.insert(f);
        audit.record(actor.getUserId(), "CREATE_TRAFFIC_FLOW", "TRAFFIC_FLOW", f.getFlowId(),
                     f.getSourceNode() + "->" + f.getDestNode());
    }

    public void delete(String id, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MONITOR_TRAFFIC);
        if (dao.delete(id) == 0) throw new ValidationException("Unknown flow: " + id);
        audit.record(actor.getUserId(), "DELETE_TRAFFIC_FLOW", "TRAFFIC_FLOW", id, null);
    }
}
