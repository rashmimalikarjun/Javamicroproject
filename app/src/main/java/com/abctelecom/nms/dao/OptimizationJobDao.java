package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.OptimizationJob;
import com.abctelecom.nms.model.OptimizationJob.Status;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class OptimizationJobDao extends BaseDao {

    private static OptimizationJob map(ResultSet rs) throws java.sql.SQLException {
        OptimizationJob j = new OptimizationJob();
        j.setJobId(rs.getInt("job_id"));
        j.setJobName(rs.getString("job_name"));
        j.setAlgorithm(rs.getString("algorithm"));
        j.setStatus(Status.valueOf(rs.getString("status")));
        j.setInputCsvPath(rs.getString("input_csv_path"));
        j.setScope(rs.getString("scope"));
        j.setSubmittedBy(rs.getInt("submitted_by"));
        Timestamp st = rs.getTimestamp("started_at");   if (st != null) j.setStartedAt(st.toLocalDateTime());
        Timestamp fi = rs.getTimestamp("finished_at");  if (fi != null) j.setFinishedAt(fi.toLocalDateTime());
        j.setErrorMessage(rs.getString("error_message"));
        Timestamp cr = rs.getTimestamp("created_at");   if (cr != null) j.setCreatedAt(cr.toLocalDateTime());
        return j;
    }

    public List<OptimizationJob> findAll() throws DaoException {
        return query("SELECT * FROM optimization_jobs ORDER BY job_id DESC", OptimizationJobDao::map);
    }

    public OptimizationJob findById(int id) throws DaoException {
        var l = query("SELECT * FROM optimization_jobs WHERE job_id = ?", OptimizationJobDao::map, id);
        return l.isEmpty() ? null : l.get(0);
    }

    public int create(OptimizationJob j) throws DaoException {
        return insertReturningKey(
            "INSERT INTO optimization_jobs (job_name, algorithm, status, input_csv_path, scope, submitted_by)" +
            " VALUES (?,?,?,?,?,?)",
            j.getJobName(), j.getAlgorithm(), j.getStatus().name(), j.getInputCsvPath(), j.getScope(), j.getSubmittedBy());
    }

    public void markRunning(int jobId) throws DaoException {
        update("UPDATE optimization_jobs SET status='RUNNING', started_at=? WHERE job_id=?",
               LocalDateTime.now(), jobId);
    }

    public void markCompleted(int jobId) throws DaoException {
        update("UPDATE optimization_jobs SET status='COMPLETED', finished_at=? WHERE job_id=?",
               LocalDateTime.now(), jobId);
    }

    public void markFailed(int jobId, String error) throws DaoException {
        String safe = error == null ? "unknown" : error.substring(0, Math.min(480, error.length()));
        update("UPDATE optimization_jobs SET status='FAILED', finished_at=?, error_message=? WHERE job_id=?",
               LocalDateTime.now(), safe, jobId);
    }
}
