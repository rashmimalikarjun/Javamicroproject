package com.abctelecom.nms.model;

import java.time.LocalDateTime;

/** Report metadata row (content lives in the exported CSV file). */
public class Report {
    private int reportId;
    private String reportType;
    private String title;
    private String csvPath;
    private String summary;
    private int generatedBy;
    private LocalDateTime generatedAt;

    public int getReportId() { return reportId; }
    public void setReportId(int v) { this.reportId = v; }
    public String getReportType() { return reportType; }
    public void setReportType(String v) { this.reportType = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getCsvPath() { return csvPath; }
    public void setCsvPath(String v) { this.csvPath = v; }
    public String getSummary() { return summary; }
    public void setSummary(String v) { this.summary = v; }
    public int getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(int v) { this.generatedBy = v; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime v) { this.generatedAt = v; }
}
