package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.ResourceAllocationDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.ResourceAllocation;
import com.abctelecom.nms.model.User;

import java.util.List;

/** Resource allocation with capacity validation (DB transaction also enforced). */
public class AllocationService {
    private final ResourceAllocationDao dao = new ResourceAllocationDao();
    private final AuditDao audit = new AuditDao();

    public List<ResourceAllocation> list() throws DaoException { return dao.findAll(); }

    public static ResourceAllocation normalize(ResourceAllocation in) throws ValidationException {
        ResourceAllocation r = new ResourceAllocation();
        r.setResourceType(Validators.oneOf(in.getResourceType(), "Resource type", Validators.RESOURCE_TYPES));
        r.setTargetEntity(Validators.id(in.getTargetEntity(), "Target entity"));
        r.setMaxValue(Validators.rangeDouble(String.valueOf(in.getMaxValue()), "Capacity ceiling", 0.01, 1e9));
        r.setAllocatedValue(Validators.rangeDouble(String.valueOf(in.getAllocatedValue()), "Allocated value", 0, 1e9));
        if (r.getAllocatedValue() > r.getMaxValue())
            throw new ValidationException("Allocation (" + r.getAllocatedValue() + ") exceeds capacity ceiling ("
                    + r.getMaxValue() + ")");
        r.setUnit(in.getUnit() == null || in.getUnit().isBlank() ? defaultUnit(r.getResourceType()) : in.getUnit().trim());
        return r;
    }

    static String defaultUnit(String type) {
        switch (type) {
            case "CPU": return "pct";
            case "MEMORY": return "GB";
            case "SPECTRUM": return "MHz";
            case "POWER": return "kW";
            default: return "users";
        }
    }

    public int allocate(ResourceAllocation in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.ALLOCATE_RESOURCES);
        ResourceAllocation r = normalize(in);
        r.setAllocatedBy(actor.getUserId());
        int id = dao.allocate(r); // throws on insufficient remaining capacity (transactional check)
        audit.record(actor.getUserId(), "ALLOCATE_RESOURCE", "ALLOCATION", String.valueOf(id),
                     r.getResourceType() + " " + r.getAllocatedValue() + " " + r.getUnit() + " -> " + r.getTargetEntity());
        return id;
    }

    public void release(int allocationId, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.ALLOCATE_RESOURCES);
        if (dao.delete(allocationId) == 0) throw new ValidationException("Unknown allocation id: " + allocationId);
        audit.record(actor.getUserId(), "RELEASE_ALLOCATION", "ALLOCATION", String.valueOf(allocationId), null);
    }
}
