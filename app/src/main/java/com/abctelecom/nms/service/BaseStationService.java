package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.BaseStationDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.BaseStation;
import com.abctelecom.nms.model.User;

import java.util.List;

/** Base station CRUD with validation and audit logging. */
public class BaseStationService {
    private final BaseStationDao dao = new BaseStationDao();
    private final AuditDao audit = new AuditDao();

    public List<BaseStation> list() throws DaoException { return dao.findAll(); }

    public BaseStation validate(BaseStation in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_BASE_STATIONS);
        BaseStation b = new BaseStation();
        b.setStationId(Validators.id(in.getStationId(), "Station ID"));
        b.setCellName(Validators.required(in.getCellName(), "Cell name"));
        b.setLocation(Validators.required(in.getLocation(), "Location"));
        b.setFrequencyBand(Validators.required(in.getFrequencyBand(), "Frequency band"));
        b.setTechnology(Validators.oneOf(in.getTechnology(), "Technology", Validators.TECHNOLOGIES));
        b.setCapacityMbps(Validators.rangeInt(String.valueOf(in.getCapacityMbps()), "Capacity (Mbps)", 1, 100_000));
        b.setCurrentLoadMbps(Validators.rangeInt(String.valueOf(in.getCurrentLoadMbps()), "Current load (Mbps)", 0, 100_000));
        b.setPowerStatus(Validators.oneOf(in.getPowerStatus(), "Power status", Validators.POWER_STATUS));
        Validators.loadWithinCapacity(b.getCurrentLoadMbps(), b.getCapacityMbps());
        return b;
    }

    public void create(BaseStation in, User actor) throws QitromException {
        BaseStation b = validate(in, actor);
        if (dao.findById(b.getStationId()) != null)
            throw new ValidationException("Station ID already exists: " + b.getStationId());
        dao.insert(b);
        audit.record(actor.getUserId(), "CREATE_BASE_STATION", "BASE_STATION", b.getStationId(),
                     "cell=" + b.getCellName());
    }

    public void update(BaseStation in, User actor) throws QitromException {
        BaseStation b = validate(in, actor);
        if (dao.findById(b.getStationId()) == null)
            throw new ValidationException("Unknown station ID: " + b.getStationId());
        dao.updateStation(b);
        audit.record(actor.getUserId(), "UPDATE_BASE_STATION", "BASE_STATION", b.getStationId(),
                     "load=" + b.getCurrentLoadMbps() + "/" + b.getCapacityMbps());
    }

    public void delete(String stationId, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_BASE_STATIONS);
        String id = Validators.id(stationId, "Station ID");
        if (dao.delete(id) == 0) throw new ValidationException("Unknown station ID: " + id);
        audit.record(actor.getUserId(), "DELETE_BASE_STATION", "BASE_STATION", id, null);
    }
}
