package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.OptimizationResult;

import java.sql.ResultSet;
import java.util.List;

public class OptimizationResultDao extends BaseDao {

    private static OptimizationResult map(ResultSet rs) throws java.sql.SQLException {
        OptimizationResult r = new OptimizationResult();
        r.setResultId(rs.getInt("result_id"));
        r.setJobId(rs.getInt("job_id"));
        r.setEntityId(rs.getString("entity_id"));
        r.setMetric(rs.getString("metric"));
        r.setBeforeValue(rs.getDouble("before_value"));
        r.setAfterValue(rs.getDouble("after_value"));
        r.setRecommendation(rs.getString("recommendation"));
        if (rs.getTimestamp("generated_at") != null) r.setGeneratedAt(rs.getTimestamp("generated_at").toLocalDateTime());
        return r;
    }

    public List<OptimizationResult> findByJob(int jobId) throws DaoException {
        return query("SELECT * FROM optimization_results WHERE job_id = ? ORDER BY result_id",
                OptimizationResultDao::map, jobId);
    }

    public void insert(OptimizationResult r) throws DaoException {
        update("INSERT INTO optimization_results (job_id, entity_id, metric, before_value, after_value, recommendation)" +
               " VALUES (?,?,?,?,?,?)",
               r.getJobId(), r.getEntityId(), r.getMetric(), r.getBeforeValue(), r.getAfterValue(), r.getRecommendation());
    }
}
