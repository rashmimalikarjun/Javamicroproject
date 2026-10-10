package com.abctelecom.nms.model;

import java.time.LocalDateTime;

/** One before/after line produced by an optimization job. */
public class OptimizationResult {
    private int resultId;
    private int jobId;
    private String entityId;
    private String metric;
    private double beforeValue;
    private double afterValue;
    private String recommendation;
    private LocalDateTime generatedAt;

    public double delta() { return afterValue - beforeValue; }

    public int getResultId() { return resultId; }
    public void setResultId(int v) { this.resultId = v; }
    public int getJobId() { return jobId; }
    public void setJobId(int v) { this.jobId = v; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String v) { this.entityId = v; }
    public String getMetric() { return metric; }
    public void setMetric(String v) { this.metric = v; }
    public double getBeforeValue() { return beforeValue; }
    public void setBeforeValue(double v) { this.beforeValue = v; }
    public double getAfterValue() { return afterValue; }
    public void setAfterValue(double v) { this.afterValue = v; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String v) { this.recommendation = v; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime v) { this.generatedAt = v; }
}
