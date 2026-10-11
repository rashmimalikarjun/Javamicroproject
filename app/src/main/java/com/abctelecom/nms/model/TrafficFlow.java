package com.abctelecom.nms.model;

import java.time.LocalDateTime;

/** Traffic flow record between two nodes. */
public class TrafficFlow {
    private String flowId;
    private String sourceNode;
    private String destNode;
    private double bandwidthMbps;
    private double latencyMs;
    private double packetLossPct;
    private double throughputMbps;
    private String congestionStatus;   // NORMAL|HIGH|CRITICAL
    private LocalDateTime recordedAt;

    public String getFlowId() { return flowId; }
    public void setFlowId(String v) { this.flowId = v; }
    public String getSourceNode() { return sourceNode; }
    public void setSourceNode(String v) { this.sourceNode = v; }
    public String getDestNode() { return destNode; }
    public void setDestNode(String v) { this.destNode = v; }
    public double getBandwidthMbps() { return bandwidthMbps; }
    public void setBandwidthMbps(double v) { this.bandwidthMbps = v; }
    public double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(double v) { this.latencyMs = v; }
    public double getPacketLossPct() { return packetLossPct; }
    public void setPacketLossPct(double v) { this.packetLossPct = v; }
    public double getThroughputMbps() { return throughputMbps; }
    public void setThroughputMbps(double v) { this.throughputMbps = v; }
    public String getCongestionStatus() { return congestionStatus; }
    public void setCongestionStatus(String v) { this.congestionStatus = v; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime v) { this.recordedAt = v; }
}
