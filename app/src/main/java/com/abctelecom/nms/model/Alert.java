package com.abctelecom.nms.model;

import java.time.LocalDateTime;

/** Alert record. Duplicate OPEN alerts per (type, entity) are prevented at the DB level. */
public class Alert {
    private int alertId;
    private String alertType;      // CELL_LOAD|LATENCY|EDGE_CPU|SLICE_LATENCY|DEVICE_OFFLINE
    private String severity;       // INFO|WARNING|CRITICAL
    private String entityId;
    private String message;
    private Double threshold;
    private Double actualValue;
    private String status = "OPEN";
    private LocalDateTime createdAt;
    private Integer acknowledgedBy;

    public int getAlertId() { return alertId; }
    public void setAlertId(int v) { this.alertId = v; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String v) { this.alertType = v; }
    public String getSeverity() { return severity; }
    public void setSeverity(String v) { this.severity = v; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String v) { this.entityId = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double v) { this.threshold = v; }
    public Double getActualValue() { return actualValue; }
    public void setActualValue(Double v) { this.actualValue = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public Integer getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(Integer v) { this.acknowledgedBy = v; }
}
