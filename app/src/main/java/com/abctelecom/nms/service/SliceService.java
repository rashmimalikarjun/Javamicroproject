package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.NetworkSliceDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.NetworkSlice;
import com.abctelecom.nms.model.User;

import java.util.List;

/** Network slice management; enforces that total active allocation <= 100%. */
public class SliceService {
    private final NetworkSliceDao dao = new NetworkSliceDao();
    private final AuditDao audit = new AuditDao();

    public List<NetworkSlice> list() throws DaoException { return dao.findAll(); }

    public static NetworkSlice normalize(NetworkSlice in) throws ValidationException {
        NetworkSlice s = new NetworkSlice();
        s.setSliceId(Validators.id(in.getSliceId(), "Slice ID"));
        s.setSliceType(Validators.oneOf(in.getSliceType(), "Slice type", Validators.SLICE_TYPES));
        s.setBandwidthMbps(Validators.rangeInt(String.valueOf(in.getBandwidthMbps()), "Bandwidth (Mbps)", 1, 1_000_000));
        s.setPriority(Validators.rangeInt(String.valueOf(in.getPriority()), "Priority", 1, 5));
        s.setLatencyRequirementMs(Validators.rangeDouble(String.valueOf(in.getLatencyRequirementMs()),
                "Latency requirement (ms)", 0.01, 1000));
        s.setResourceAllocationPct(Validators.rangeDouble(String.valueOf(in.getResourceAllocationPct()),
                "Resource allocation (%)", 0, 100));
        s.setStatus(in.getStatus() == null || in.getStatus().isBlank() ? "ACTIVE" : in.getStatus());
        return s;
    }

    public void create(NetworkSlice in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SLICES);
        NetworkSlice s = normalize(in);
        if (list().stream().anyMatch(x -> x.getSliceId().equals(s.getSliceId())))
            throw new ValidationException("Slice ID already exists: " + s.getSliceId());
        double used = dao.totalAllocationPct();
        if ("ACTIVE".equals(s.getStatus()) && used + s.getResourceAllocationPct() > 100.0)
            throw new ValidationException("Slice pool exhausted: " + used + "% already allocated; requested "
                    + s.getResourceAllocationPct() + "% would exceed 100%");
        dao.insert(s);
        audit.record(actor.getUserId(), "CREATE_SLICE", "SLICE", s.getSliceId(),
                     "type=" + s.getSliceType() + " alloc=" + s.getResourceAllocationPct() + "%");
    }

    public void update(NetworkSlice in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SLICES);
        NetworkSlice s = normalize(in);
        double others = dao.totalAllocationPct();
        for (NetworkSlice old : list())
            if (old.getSliceId().equals(s.getSliceId()) && "ACTIVE".equals(old.getStatus()))
                others -= old.getResourceAllocationPct();
        if ("ACTIVE".equals(s.getStatus()) && others + s.getResourceAllocationPct() > 100.0)
            throw new ValidationException("Update would exceed 100% slice resource pool");
        if (dao.updateSlice(s) == 0) throw new ValidationException("Unknown slice: " + s.getSliceId());
        audit.record(actor.getUserId(), "UPDATE_SLICE", "SLICE", s.getSliceId(), null);
    }

    public void delete(String id, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SLICES);
        if (dao.delete(id) == 0) throw new ValidationException("Unknown slice: " + id);
        audit.record(actor.getUserId(), "DELETE_SLICE", "SLICE", id, null);
    }
}
