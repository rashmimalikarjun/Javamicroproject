package com.abctelecom.nms.model;

/** Network slice. */
public class NetworkSlice {
    private String sliceId;
    private String sliceType;          // eMBB|URLLC|mMTC
    private int bandwidthMbps;
    private int priority;              // 1..5
    private double latencyRequirementMs;
    private double resourceAllocationPct;
    private String status = "ACTIVE";

    public String getSliceId() { return sliceId; }
    public void setSliceId(String v) { this.sliceId = v; }
    public String getSliceType() { return sliceType; }
    public void setSliceType(String v) { this.sliceType = v; }
    public int getBandwidthMbps() { return bandwidthMbps; }
    public void setBandwidthMbps(int v) { this.bandwidthMbps = v; }
    public int getPriority() { return priority; }
    public void setPriority(int v) { this.priority = v; }
    public double getLatencyRequirementMs() { return latencyRequirementMs; }
    public void setLatencyRequirementMs(double v) { this.latencyRequirementMs = v; }
    public double getResourceAllocationPct() { return resourceAllocationPct; }
    public void setResourceAllocationPct(double v) { this.resourceAllocationPct = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
