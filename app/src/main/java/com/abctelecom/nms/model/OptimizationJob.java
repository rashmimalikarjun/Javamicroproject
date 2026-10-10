package com.abctelecom.nms.model;

import java.time.LocalDateTime;

/** Optimization job (local deterministic simulation, NOT quantum / NOT QpiAI Opt). */
public class OptimizationJob {
    public enum Status { PENDING, RUNNING, COMPLETED, FAILED }

    private int jobId;
    private String jobName;
    private String algorithm = "LOCAL_GREEDY_SIMULATION";
    private Status status = Status.PENDING;
    private String inputCsvPath;
    private String scope;
    private int submittedBy;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String errorMessage;
    private LocalDateTime createdAt;

    public int getJobId() { return jobId; }
    public void setJobId(int v) { this.jobId = v; }
    public String getJobName() { return jobName; }
    public void setJobName(String v) { this.jobName = v; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String v) { this.algorithm = v; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { this.status = v; }
    public String getInputCsvPath() { return inputCsvPath; }
    public void setInputCsvPath(String v) { this.inputCsvPath = v; }
    public String getScope() { return scope; }
    public void setScope(String v) { this.scope = v; }
    public int getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(int v) { this.submittedBy = v; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime v) { this.startedAt = v; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime v) { this.finishedAt = v; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String v) { this.errorMessage = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
