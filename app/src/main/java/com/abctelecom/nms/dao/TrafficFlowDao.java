package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.TrafficFlow;

import java.sql.ResultSet;
import java.util.List;

public class TrafficFlowDao extends BaseDao {

    private static TrafficFlow map(ResultSet rs) throws java.sql.SQLException {
        TrafficFlow f = new TrafficFlow();
        f.setFlowId(rs.getString("flow_id"));
        f.setSourceNode(rs.getString("source_node"));
        f.setDestNode(rs.getString("dest_node"));
        f.setBandwidthMbps(rs.getDouble("bandwidth_mbps"));
        f.setLatencyMs(rs.getDouble("latency_ms"));
        f.setPacketLossPct(rs.getDouble("packet_loss_pct"));
        f.setThroughputMbps(rs.getDouble("throughput_mbps"));
        f.setCongestionStatus(rs.getString("congestion_status"));
        f.setRecordedAt(rs.getTimestamp("recorded_at").toLocalDateTime());
        return f;
    }

    public List<TrafficFlow> findAll() throws DaoException {
        return query("SELECT * FROM traffic_flows ORDER BY recorded_at DESC, flow_id", TrafficFlowDao::map);
    }

    public void insert(TrafficFlow f) throws DaoException {
        update("INSERT INTO traffic_flows (flow_id, source_node, dest_node, bandwidth_mbps, latency_ms," +
               " packet_loss_pct, throughput_mbps, congestion_status) VALUES (?,?,?,?,?,?,?,?)",
               f.getFlowId(), f.getSourceNode(), f.getDestNode(), f.getBandwidthMbps(), f.getLatencyMs(),
               f.getPacketLossPct(), f.getThroughputMbps(), f.getCongestionStatus());
    }

    public int delete(String id) throws DaoException {
        return update("DELETE FROM traffic_flows WHERE flow_id = ?", id);
    }

    public double avgLatencyMs() throws DaoException {
        return query("SELECT COALESCE(AVG(latency_ms),0) v FROM traffic_flows", rs -> rs.getDouble("v")).get(0);
    }

    public double avgPacketLossPct() throws DaoException {
        return query("SELECT COALESCE(AVG(packet_loss_pct),0) v FROM traffic_flows", rs -> rs.getDouble("v")).get(0);
    }

    public double totalThroughputMbps() throws DaoException {
        return query("SELECT COALESCE(SUM(throughput_mbps),0) v FROM traffic_flows", rs -> rs.getDouble("v")).get(0);
    }
}
