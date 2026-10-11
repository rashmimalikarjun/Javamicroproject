package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.BaseStationDao;
import com.abctelecom.nms.dao.DeviceDao;
import com.abctelecom.nms.dao.SubscriberDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.Device;
import com.abctelecom.nms.model.Subscriber;
import com.abctelecom.nms.model.User;

import java.util.List;

/** Subscriber & device management with referential checks. */
public class SubscriberService {
    private final SubscriberDao subs = new SubscriberDao();
    private final DeviceDao devs = new DeviceDao();
    private final BaseStationDao stations = new BaseStationDao();
    private final AuditDao audit = new AuditDao();

    // ---- subscribers -------------------------------------------------
    public List<Subscriber> listSubscribers() throws DaoException { return subs.findAll(); }

    public void createSubscriber(Subscriber in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SUBSCRIBERS_DEVICES);
        Subscriber s = normalizeSub(in);
        if (subs.findById(s.getSubscriberId()) != null)
            throw new ValidationException("Subscriber ID already exists: " + s.getSubscriberId());
        subs.insert(s);
        audit.record(actor.getUserId(), "CREATE_SUBSCRIBER", "SUBSCRIBER", s.getSubscriberId(), null);
    }

    public void updateSubscriber(Subscriber in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SUBSCRIBERS_DEVICES);
        Subscriber s = normalizeSub(in);
        if (subs.updateSubscriber(s) == 0) throw new ValidationException("Unknown subscriber: " + s.getSubscriberId());
        audit.record(actor.getUserId(), "UPDATE_SUBSCRIBER", "SUBSCRIBER", s.getSubscriberId(), null);
    }

    public void deleteSubscriber(String id, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SUBSCRIBERS_DEVICES);
        if (subs.delete(id) == 0) throw new ValidationException("Unknown subscriber: " + id);
        audit.record(actor.getUserId(), "DELETE_SUBSCRIBER", "SUBSCRIBER", id, null);
    }

    private Subscriber normalizeSub(Subscriber in) throws QitromException {
        Subscriber s = new Subscriber();
        s.setSubscriberId(Validators.id(in.getSubscriberId(), "Subscriber ID"));
        s.setName(Validators.required(in.getName(), "Name"));
        s.setServicePlan(Validators.oneOf(in.getServicePlan(), "Service plan", Validators.SERVICE_PLANS));
        s.setQosLevel(Validators.rangeInt(String.valueOf(in.getQosLevel()), "QoS level", 1, 5));
        s.setDataUsageMb(Validators.rangeInt(String.valueOf(in.getDataUsageMb()), "Data usage (MB)", 0, Integer.MAX_VALUE));
        String cell = in.getConnectedCell();
        if (cell != null && !cell.isBlank()) {
            if (stations.findById(cell) == null) throw new ValidationException("Unknown connected cell: " + cell);
            s.setConnectedCell(cell);
        }
        return s;
    }

    // ---- devices -----------------------------------------------------
    public List<Device> listDevices() throws DaoException { return devs.findAll(); }

    public void createDevice(Device in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SUBSCRIBERS_DEVICES);
        Device d = normalizeDev(in);
        if (devs.findAll().stream().anyMatch(x -> x.getDeviceId().equals(d.getDeviceId())))
            throw new ValidationException("Device ID already exists: " + d.getDeviceId());
        devs.insert(d);
        audit.record(actor.getUserId(), "CREATE_DEVICE", "DEVICE", d.getDeviceId(), null);
    }

    public void updateDevice(Device in, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SUBSCRIBERS_DEVICES);
        Device d = normalizeDev(in);
        if (devs.updateDevice(d) == 0) throw new ValidationException("Unknown device: " + d.getDeviceId());
        audit.record(actor.getUserId(), "UPDATE_DEVICE", "DEVICE", d.getDeviceId(), null);
    }

    public void deleteDevice(String id, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.MANAGE_SUBSCRIBERS_DEVICES);
        if (devs.delete(id) == 0) throw new ValidationException("Unknown device: " + id);
        audit.record(actor.getUserId(), "DELETE_DEVICE", "DEVICE", id, null);
    }

    private Device normalizeDev(Device in) throws QitromException {
        Device d = new Device();
        d.setDeviceId(Validators.id(in.getDeviceId(), "Device ID"));
        d.setSubscriberId(Validators.id(in.getSubscriberId(), "Subscriber ID"));
        if (subs.findById(d.getSubscriberId()) == null)
            throw new ValidationException("Unknown subscriber: " + d.getSubscriberId());
        d.setDeviceType(Validators.oneOf(in.getDeviceType(), "Device type", Validators.DEVICE_TYPES));
        d.setSignalStrengthDbm(Validators.rangeInt(String.valueOf(in.getSignalStrengthDbm()),
                "Signal strength (dBm)", -160, 0));
        d.setStatus(Validators.oneOf(in.getStatus(), "Status", Validators.DEVICE_STATUS));
        String cell = in.getConnectedCell();
        if (cell != null && !cell.isBlank()) {
            if (stations.findById(cell) == null) throw new ValidationException("Unknown connected cell: " + cell);
            d.setConnectedCell(cell);
        }
        return d;
    }
}
