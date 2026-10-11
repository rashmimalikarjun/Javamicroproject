package com.abctelecom.nms.dao;

import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.Report;

import java.sql.ResultSet;
import java.util.List;

public class ReportDao extends BaseDao {

    private static Report map(ResultSet rs) throws java.sql.SQLException {
        Report r = new Report();
        r.setReportId(rs.getInt("report_id"));
        r.setReportType(rs.getString("report_type"));
        r.setTitle(rs.getString("title"));
        r.setCsvPath(rs.getString("csv_path"));
        r.setSummary(rs.getString("summary"));
        r.setGeneratedBy(rs.getInt("generated_by"));
        if (rs.getTimestamp("generated_at") != null) r.setGeneratedAt(rs.getTimestamp("generated_at").toLocalDateTime());
        return r;
    }

    public List<Report> findAll() throws DaoException {
        return query("SELECT * FROM reports ORDER BY generated_at DESC, report_id DESC", ReportDao::map);
    }

    public int create(Report r) throws DaoException {
        return insertReturningKey(
            "INSERT INTO reports (report_type, title, csv_path, summary, generated_by) VALUES (?,?,?,?,?)",
            r.getReportType(), r.getTitle(), r.getCsvPath(), r.getSummary(), r.getGeneratedBy());
    }
}
