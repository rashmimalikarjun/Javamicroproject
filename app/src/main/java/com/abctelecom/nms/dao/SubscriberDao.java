package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.Subscriber;

import java.sql.ResultSet;
import java.util.List;

public class SubscriberDao extends BaseDao {

    private static Subscriber map(ResultSet rs) throws java.sql.SQLException {
        Subscriber s = new Subscriber();
        s.setSubscriberId(rs.getString("subscriber_id"));
        s.setName(rs.getString("name"));
        s.setServicePlan(rs.getString("service_plan"));
        s.setQosLevel(rs.getInt("qos_level"));
        s.setConnectedCell(rs.getString("connected_cell"));
        s.setDataUsageMb(rs.getInt("data_usage_mb"));
        return s;
    }

    public List<Subscriber> findAll() throws DaoException {
        return query("SELECT * FROM subscribers ORDER BY subscriber_id", SubscriberDao::map);
    }

    public Subscriber findById(String id) throws DaoException {
        var l = query("SELECT * FROM subscribers WHERE subscriber_id = ?", SubscriberDao::map, id);
        return l.isEmpty() ? null : l.get(0);
    }

    public void insert(Subscriber s) throws DaoException {
        update("INSERT INTO subscribers (subscriber_id, name, service_plan, qos_level, connected_cell, data_usage_mb)" +
               " VALUES (?,?,?,?,?,?)",
               s.getSubscriberId(), s.getName(), s.getServicePlan(), s.getQosLevel(),
               s.getConnectedCell(), s.getDataUsageMb());
    }

    public int updateSubscriber(Subscriber s) throws DaoException {
        return update("UPDATE subscribers SET name=?, service_plan=?, qos_level=?, connected_cell=?, data_usage_mb=?" +
               " WHERE subscriber_id=?",
               s.getName(), s.getServicePlan(), s.getQosLevel(), s.getConnectedCell(),
               s.getDataUsageMb(), s.getSubscriberId());
    }

    public int delete(String id) throws DaoException {
        return update("DELETE FROM subscribers WHERE subscriber_id = ?", id);
    }

    public int countActiveUsers() throws DaoException {
        return query("SELECT COUNT(*) c FROM subscribers WHERE connected_cell IS NOT NULL",
                rs -> rs.getInt("c")).get(0);
    }
}
