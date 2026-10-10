package com.abctelecom.nms.model;

import java.time.LocalDateTime;

/** Stored KPI snapshot. isSimulated=true marks values that are simulated, not measured. */
public class NetworkKpi {
    private int kpiId;
    private String kpiName;
    private double kpiValue;
    private String unit;
    private boolean simulated;
    private LocalDateTime snapshotAt;

    public NetworkKpi() {}
    public NetworkKpi(String kpiName, double kpiValue, String unit, boolean simulated) {
        this.kpiName = kpiName; this.kpiValue = kpiValue; this.unit = unit; this.simulated = simulated;
    }

    public int getKpiId() { return kpiId; }
    public void setKpiId(int v) { this.kpiId = v; }
    public String getKpiName() { return kpiName; }
    public void setKpiName(String v) { this.kpiName = v; }
    public double getKpiValue() { return kpiValue; }
    public void setKpiValue(double v) { this.kpiValue = v; }
    public String getUnit() { return unit; }
    public void setUnit(String v) { this.unit = v; }
    public boolean isSimulated() { return simulated; }
    public void setSimulated(boolean v) { this.simulated = v; }
    public LocalDateTime getSnapshotAt() { return snapshotAt; }
    public void setSnapshotAt(LocalDateTime v) { this.snapshotAt = v; }
}
