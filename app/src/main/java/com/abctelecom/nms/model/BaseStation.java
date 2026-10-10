package com.abctelecom.nms.model;

/** Base station (cell site). */
public class BaseStation {
    private String stationId;     // BS-001
    private String cellName;
    private String location;
    private String frequencyBand;
    private String technology;    // 4G | 5G | 6G
    private int capacityMbps;
    private int currentLoadMbps;
    private String powerStatus;   // ON | OFF | SLEEP

    public double utilizationPct() {
        if (capacityMbps <= 0) return 0;
        return Math.min(100.0, currentLoadMbps * 100.0 / capacityMbps);
    }

    public String getStationId() { return stationId; }
    public void setStationId(String v) { this.stationId = v; }
    public String getCellName() { return cellName; }
    public void setCellName(String v) { this.cellName = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public String getFrequencyBand() { return frequencyBand; }
    public void setFrequencyBand(String v) { this.frequencyBand = v; }
    public String getTechnology() { return technology; }
    public void setTechnology(String v) { this.technology = v; }
    public int getCapacityMbps() { return capacityMbps; }
    public void setCapacityMbps(int v) { this.capacityMbps = v; }
    public int getCurrentLoadMbps() { return currentLoadMbps; }
    public void setCurrentLoadMbps(int v) { this.currentLoadMbps = v; }
    public String getPowerStatus() { return powerStatus; }
    public void setPowerStatus(String v) { this.powerStatus = v; }
}
