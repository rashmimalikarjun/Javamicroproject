package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.NetworkSlice;

import java.sql.ResultSet;
import java.util.List;

public class NetworkSliceDao extends BaseDao {

    private static NetworkSlice map(ResultSet rs) throws java.sql.SQLException {
        NetworkSlice s = new NetworkSlice();
        s.setSliceId(rs.getString("slice_id"));
        s.setSliceType(rs.getString("slice_type"));
        s.setBandwidthMbps(rs.getInt("bandwidth_allocation_mbps"));
        s.setPriority(rs.getInt("priority"));
        s.setLatencyRequirementMs(rs.getDouble("latency_requirement_ms"));
        s.setResourceAllocationPct(rs.getDouble("resource_allocation_pct"));
        s.setStatus(rs.getString("status"));
        return s;
    }

    public List<NetworkSlice> findAll() throws DaoException {
        return query("SELECT * FROM network_slices ORDER BY slice_id", NetworkSliceDao::map);
    }

    public void insert(NetworkSlice s) throws DaoException {
        update("INSERT INTO network_slices (slice_id, slice_type, bandwidth_allocation_mbps, priority," +
               " latency_requirement_ms, resource_allocation_pct, status) VALUES (?,?,?,?,?,?,?)",
               s.getSliceId(), s.getSliceType(), s.getBandwidthMbps(), s.getPriority(),
               s.getLatencyRequirementMs(), s.getResourceAllocationPct(), s.getStatus());
    }

    public int updateSlice(NetworkSlice s) throws DaoException {
        return update("UPDATE network_slices SET slice_type=?, bandwidth_allocation_mbps=?, priority=?," +
               " latency_requirement_ms=?, resource_allocation_pct=?, status=? WHERE slice_id=?",
               s.getSliceType(), s.getBandwidthMbps(), s.getPriority(),
               s.getLatencyRequirementMs(), s.getResourceAllocationPct(), s.getStatus(), s.getSliceId());
    }

    public int delete(String id) throws DaoException {
        return update("DELETE FROM network_slices WHERE slice_id = ?", id);
    }

    /** Sum of allocated resource percentages — used to enforce pool capacity. */
    public double totalAllocationPct() throws DaoException {
        return query("SELECT COALESCE(SUM(resource_allocation_pct),0) t FROM network_slices WHERE status='ACTIVE'",
                rs -> rs.getDouble("t")).get(0);
    }
}
