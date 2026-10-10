package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.EdgeServerDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.EdgeServer;
import com.abctelecom.nms.model.User;

import java.util.List;

/** Edge server management. */
public class EdgeServerService {
    private final EdgeServerDao dao = new EdgeServerDao();
    private final AuditDao audit = new AuditDao();

    public List<EdgeServer> list() throws DaoException { return dao.findAll(); }

    public static EdgeServer normalize(EdgeServer in) throws ValidationException {
        EdgeServer e = new EdgeServer();
        e.setServerId(Validators.id(in.getServerId(), "Server ID"));
        e.setSiteLocation(Validators.required(in.getSiteLocation(), "Site location"));
        e.setCpuUtilizationPct(Validators.rangeDouble(String.valueOf(in.getCpuUtilizationPct()), "CPU %", 0, 100));
        e.setMemoryTotalGb(Validators.rangeDouble(String.valueOf(in.getMemoryTotalGb()), "Memory total (GB)", 0.1, 100_000));
        e.setMemoryUsedGb(Validators.rangeDouble(String.valueOf(in.getMemoryUsedGb()), "Memory used (GB)", 0, 100_000));
        if (e.getMemoryUsedGb() > e.getMemoryTotalGb())
            throw new ValidationException("Memory used exceeds memory total");
        e.setStorageTotalTb(Validators.rangeDouble(String.valueOf(in.getStorageTotalTb()), "Storage total (TB)", 0.1, 100_000));
        e.setStorageUsedTb(Validators.rangeDouble(String.valueOf(in.getStorageUsedTb()), "Storage used (TB)", 0, 100_000));
        if (e.getStorageUsedTb() > e.getStorageTotalTb())
            throw new ValidationException("Storage used exceeds storage total");
        e.setEdgeLoadPct(Validators.rangeDouble(String.valueOf(in.getEdgeLoadPct()), "Edge load %", 0, 100));
        e.setStatus(Validators.oneOf(in.getStatus(), "Status", Validators.EDGE_STATUS));
        e.setRunningApps(in.getRunningApps() == null ? "" : in.getRunningApps().trim());
        return e;
    }

    public void create(EdgeServer in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_EDGE_SERVERS);
        EdgeServer e = normalize(in);
        if (list().stream().anyMatch(x -> x.getServerId().equals(e.getServerId())))
            throw new ValidationException("Edge server ID already exists: " + e.getServerId());
        dao.insert(e);
        audit.record(actor.getUserId(), "CREATE_EDGE_SERVER", "EDGE_SERVER", e.getServerId(), null);
    }

    public void update(EdgeServer in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_EDGE_SERVERS);
        EdgeServer e = normalize(in);
        if (dao.updateServer(e) == 0) throw new ValidationException("Unknown edge server: " + e.getServerId());
        audit.record(actor.getUserId(), "UPDATE_EDGE_SERVER", "EDGE_SERVER", e.getServerId(), null);
    }

    public void delete(String id, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_EDGE_SERVERS);
        if (dao.delete(id) == 0) throw new ValidationException("Unknown edge server: " + id);
        audit.record(actor.getUserId(), "DELETE_EDGE_SERVER", "EDGE_SERVER", id, null);
    }
}
