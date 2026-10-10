package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.ResourceAllocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ResourceAllocationDao extends BaseDao {

    private static ResourceAllocation map(ResultSet rs) throws SQLException {
        ResourceAllocation r = new ResourceAllocation();
        r.setAllocationId(rs.getInt("allocation_id"));
        r.setResourceType(rs.getString("resource_type"));
        r.setTargetEntity(rs.getString("target_entity"));
        r.setAllocatedValue(rs.getDouble("allocated_value"));
        r.setUnit(rs.getString("unit"));
        r.setMaxValue(rs.getDouble("max_value"));
        r.setAllocatedBy(rs.getInt("allocated_by"));
        if (rs.getTimestamp("allocated_at") != null) r.setAllocatedAt(rs.getTimestamp("allocated_at").toLocalDateTime());
        return r;
    }

    public List<ResourceAllocation> findAll() throws DaoException {
        return query("SELECT * FROM resource_allocations ORDER BY allocation_id", ResourceAllocationDao::map);
    }

    /**
     * Transactional capacity-checked allocation: the row-level ceiling check
     * (allocated_value <= max_value) is enforced here AND by a DB CHECK constraint.
     */
    public int allocate(ResourceAllocation r) throws DaoException {
        Connection conn = null;
        try {
            conn = open();
            conn.setAutoCommit(false);
            // Sum of existing allocations for this (entity,type) must fit with the new one inside max_value.
            double existing;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(allocated_value),0) s FROM resource_allocations" +
                    " WHERE target_entity=? AND resource_type=?")) {
                ps.setString(1, r.getTargetEntity());
                ps.setString(2, r.getResourceType());
                try (ResultSet rs = ps.executeQuery()) { rs.next(); existing = rs.getDouble("s"); }
            }
            if (existing + r.getAllocatedValue() > r.getMaxValue()) {
                conn.rollback();
                throw new DaoException("Insufficient capacity: " + r.getResourceType() +
                        " on " + r.getTargetEntity() + " already has " + existing + " of " + r.getMaxValue() + " " + r.getUnit());
            }
            int id;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO resource_allocations (resource_type, target_entity, allocated_value, unit, max_value, allocated_by)" +
                    " VALUES (?,?,?,?,?,?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, r.getResourceType());
                ps.setString(2, r.getTargetEntity());
                ps.setDouble(3, r.getAllocatedValue());
                ps.setString(4, r.getUnit());
                ps.setDouble(5, r.getMaxValue());
                ps.setInt(6, r.getAllocatedBy());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { id = rs.next() ? rs.getInt(1) : -1; }
            }
            conn.commit();
            return id;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new DaoException("Allocation failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    public int delete(int allocationId) throws DaoException {
        return update("DELETE FROM resource_allocations WHERE allocation_id = ?", allocationId);
    }
}
