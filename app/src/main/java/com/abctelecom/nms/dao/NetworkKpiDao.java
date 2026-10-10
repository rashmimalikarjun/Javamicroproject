package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.NetworkKpi;

import java.sql.ResultSet;
import java.util.List;

public class NetworkKpiDao extends BaseDao {

    private static NetworkKpi map(ResultSet rs) throws java.sql.SQLException {
        NetworkKpi k = new NetworkKpi();
        k.setKpiId(rs.getInt("kpi_id"));
        k.setKpiName(rs.getString("kpi_name"));
        k.setKpiValue(rs.getDouble("kpi_value"));
        k.setUnit(rs.getString("unit"));
        k.setSimulated(rs.getBoolean("is_simulated"));
        if (rs.getTimestamp("snapshot_at") != null) k.setSnapshotAt(rs.getTimestamp("snapshot_at").toLocalDateTime());
        return k;
    }

    public void insert(NetworkKpi k) throws DaoException {
        update("INSERT INTO network_kpis (kpi_name, kpi_value, unit, is_simulated) VALUES (?,?,?,?)",
               k.getKpiName(), k.getKpiValue(), k.getUnit(), k.isSimulated());
    }

    /** Latest snapshot per KPI name. */
    public List<NetworkKpi> latestSnapshots() throws DaoException {
        return query("SELECT k.* FROM network_kpis k JOIN (" +
                     "  SELECT kpi_name, MAX(snapshot_at) m FROM network_kpis GROUP BY kpi_name" +
                     ") t ON t.kpi_name = k.kpi_name AND t.m = k.snapshot_at ORDER BY k.kpi_name",
                     NetworkKpiDao::map);
    }
}
