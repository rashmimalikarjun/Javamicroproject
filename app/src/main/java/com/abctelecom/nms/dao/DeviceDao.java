package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.Device;

import java.sql.ResultSet;
import java.util.List;

public class DeviceDao extends BaseDao {

    private static Device map(ResultSet rs) throws java.sql.SQLException {
        Device d = new Device();
        d.setDeviceId(rs.getString("device_id"));
        d.setSubscriberId(rs.getString("subscriber_id"));
        d.setDeviceType(rs.getString("device_type"));
        d.setSignalStrengthDbm(rs.getInt("signal_strength_dbm"));
        d.setConnectedCell(rs.getString("connected_cell"));
        d.setStatus(rs.getString("status"));
        return d;
    }

    public List<Device> findAll() throws DaoException {
        return query("SELECT * FROM devices ORDER BY device_id", DeviceDao::map);
    }

    public void insert(Device d) throws DaoException {
        update("INSERT INTO devices (device_id, subscriber_id, device_type, signal_strength_dbm, connected_cell, status)" +
               " VALUES (?,?,?,?,?,?)",
               d.getDeviceId(), d.getSubscriberId(), d.getDeviceType(),
               d.getSignalStrengthDbm(), d.getConnectedCell(), d.getStatus());
    }

    public int updateDevice(Device d) throws DaoException {
        return update("UPDATE devices SET subscriber_id=?, device_type=?, signal_strength_dbm=?," +
               " connected_cell=?, status=? WHERE device_id=?",
               d.getSubscriberId(), d.getDeviceType(), d.getSignalStrengthDbm(),
               d.getConnectedCell(), d.getStatus(), d.getDeviceId());
    }

    public int delete(String id) throws DaoException {
        return update("DELETE FROM devices WHERE device_id = ?", id);
    }
}
