package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.BaseStation;

import java.sql.ResultSet;
import java.util.List;

public class BaseStationDao extends BaseDao {

    private static BaseStation map(ResultSet rs) throws java.sql.SQLException {
        BaseStation b = new BaseStation();
        b.setStationId(rs.getString("station_id"));
        b.setCellName(rs.getString("cell_name"));
        b.setLocation(rs.getString("location"));
        b.setFrequencyBand(rs.getString("frequency_band"));
        b.setTechnology(rs.getString("technology"));
        b.setCapacityMbps(rs.getInt("capacity_mbps"));
        b.setCurrentLoadMbps(rs.getInt("current_load_mbps"));
        b.setPowerStatus(rs.getString("power_status"));
        return b;
    }

    public List<BaseStation> findAll() throws DaoException {
        return query("SELECT * FROM base_stations ORDER BY station_id", BaseStationDao::map);
    }

    public BaseStation findById(String id) throws DaoException {
        List<BaseStation> l = query("SELECT * FROM base_stations WHERE station_id = ?", BaseStationDao::map, id);
        return l.isEmpty() ? null : l.get(0);
    }

    public void insert(BaseStation b) throws DaoException {
        update("INSERT INTO base_stations (station_id, cell_name, location, frequency_band, technology," +
               " capacity_mbps, current_load_mbps, power_status) VALUES (?,?,?,?,?,?,?,?)",
               b.getStationId(), b.getCellName(), b.getLocation(), b.getFrequencyBand(), b.getTechnology(),
               b.getCapacityMbps(), b.getCurrentLoadMbps(), b.getPowerStatus());
    }

    public int updateStation(BaseStation b) throws DaoException {
        return update("UPDATE base_stations SET cell_name=?, location=?, frequency_band=?, technology=?," +
               " capacity_mbps=?, current_load_mbps=?, power_status=? WHERE station_id=?",
               b.getCellName(), b.getLocation(), b.getFrequencyBand(), b.getTechnology(),
               b.getCapacityMbps(), b.getCurrentLoadMbps(), b.getPowerStatus(), b.getStationId());
    }

    public int delete(String id) throws DaoException {
        return update("DELETE FROM base_stations WHERE station_id = ?", id);
    }

    /** Aggregate used by KPI service. */
    public double totalUtilizationPct() throws DaoException {
        List<Double> v = query("SELECT COALESCE(SUM(current_load_mbps)*100.0/NULLIF(SUM(capacity_mbps),0),0) AS u" +
                " FROM base_stations WHERE power_status='ON'", rs -> rs.getDouble("u"));
        return v.isEmpty() ? 0 : v.get(0);
    }

    public int countActive() throws DaoException {
        return query("SELECT COUNT(*) c FROM base_stations WHERE power_status='ON'", rs -> rs.getInt("c")).get(0);
    }

    public int countAll() throws DaoException {
        return query("SELECT COUNT(*) c FROM base_stations", rs -> rs.getInt("c")).get(0);
    }
}
