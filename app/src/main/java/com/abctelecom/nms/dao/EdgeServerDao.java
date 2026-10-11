package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.EdgeServer;

import java.sql.ResultSet;
import java.util.List;

public class EdgeServerDao extends BaseDao {

    private static EdgeServer map(ResultSet rs) throws java.sql.SQLException {
        EdgeServer e = new EdgeServer();
        e.setServerId(rs.getString("server_id"));
        e.setSiteLocation(rs.getString("site_location"));
        e.setCpuUtilizationPct(rs.getDouble("cpu_utilization_pct"));
        e.setMemoryUsedGb(rs.getDouble("memory_used_gb"));
        e.setMemoryTotalGb(rs.getDouble("memory_total_gb"));
        e.setStorageTotalTb(rs.getDouble("storage_total_tb"));
        e.setStorageUsedTb(rs.getDouble("storage_used_tb"));
        e.setRunningApps(rs.getString("running_apps"));
        e.setEdgeLoadPct(rs.getDouble("edge_load_pct"));
        e.setStatus(rs.getString("status"));
        return e;
    }

    public List<EdgeServer> findAll() throws DaoException {
        return query("SELECT * FROM edge_servers ORDER BY server_id", EdgeServerDao::map);
    }

    public void insert(EdgeServer e) throws DaoException {
        update("INSERT INTO edge_servers (server_id, site_location, cpu_utilization_pct, memory_used_gb," +
               " memory_total_gb, storage_total_tb, storage_used_tb, running_apps, edge_load_pct, status)" +
               " VALUES (?,?,?,?,?,?,?,?,?,?)",
               e.getServerId(), e.getSiteLocation(), e.getCpuUtilizationPct(), e.getMemoryUsedGb(),
               e.getMemoryTotalGb(), e.getStorageTotalTb(), e.getStorageUsedTb(),
               e.getRunningApps(), e.getEdgeLoadPct(), e.getStatus());
    }

    public int updateServer(EdgeServer e) throws DaoException {
        return update("UPDATE edge_servers SET site_location=?, cpu_utilization_pct=?, memory_used_gb=?," +
               " memory_total_gb=?, storage_total_tb=?, storage_used_tb=?, running_apps=?, edge_load_pct=?, status=?" +
               " WHERE server_id=?",
               e.getSiteLocation(), e.getCpuUtilizationPct(), e.getMemoryUsedGb(),
               e.getMemoryTotalGb(), e.getStorageTotalTb(), e.getStorageUsedTb(),
               e.getRunningApps(), e.getEdgeLoadPct(), e.getStatus(), e.getServerId());
    }

    public int delete(String id) throws DaoException {
        return update("DELETE FROM edge_servers WHERE server_id = ?", id);
    }
}
