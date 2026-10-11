package com.abctelecom.nms.model;

/** Edge server. */
public class EdgeServer {
    private String serverId;           // EDGE-01
    private String siteLocation;
    private double cpuUtilizationPct;
    private double memoryUsedGb;
    private double memoryTotalGb;
    private double storageTotalTb;
    private double storageUsedTb;
    private String runningApps;        // ";" separated
    private double edgeLoadPct;
    private String status = "ONLINE";  // ONLINE|OFFLINE|MAINTENANCE

    public double memoryPct() { return memoryTotalGb <= 0 ? 0 : memoryUsedGb * 100.0 / memoryTotalGb; }
    public double storagePct() { return storageTotalTb <= 0 ? 0 : storageUsedTb * 100.0 / storageTotalTb; }

    public String getServerId() { return serverId; }
    public void setServerId(String v) { this.serverId = v; }
    public String getSiteLocation() { return siteLocation; }
    public void setSiteLocation(String v) { this.siteLocation = v; }
    public double getCpuUtilizationPct() { return cpuUtilizationPct; }
    public void setCpuUtilizationPct(double v) { this.cpuUtilizationPct = v; }
    public double getMemoryUsedGb() { return memoryUsedGb; }
    public void setMemoryUsedGb(double v) { this.memoryUsedGb = v; }
    public double getMemoryTotalGb() { return memoryTotalGb; }
    public void setMemoryTotalGb(double v) { this.memoryTotalGb = v; }
    public double getStorageTotalTb() { return storageTotalTb; }
    public void setStorageTotalTb(double v) { this.storageTotalTb = v; }
    public double getStorageUsedTb() { return storageUsedTb; }
    public void setStorageUsedTb(double v) { this.storageUsedTb = v; }
    public String getRunningApps() { return runningApps; }
    public void setRunningApps(String v) { this.runningApps = v; }
    public double getEdgeLoadPct() { return edgeLoadPct; }
    public void setEdgeLoadPct(double v) { this.edgeLoadPct = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
