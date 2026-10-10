package com.abctelecom.nms.model;

/** Device entity (UE/CPE/IoT). */
public class Device {
    private String deviceId;       // DEV-00001
    private String subscriberId;
    private String deviceType;     // SMARTPHONE|CPE|IOT_SENSOR|ROUTER|TABLET|WEARABLE
    private int signalStrengthDbm; // negative dBm
    private String connectedCell;
    private String status;         // ACTIVE|IDLE|OFFLINE

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String v) { this.deviceId = v; }
    public String getSubscriberId() { return subscriberId; }
    public void setSubscriberId(String v) { this.subscriberId = v; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String v) { this.deviceType = v; }
    public int getSignalStrengthDbm() { return signalStrengthDbm; }
    public void setSignalStrengthDbm(int v) { this.signalStrengthDbm = v; }
    public String getConnectedCell() { return connectedCell; }
    public void setConnectedCell(String v) { this.connectedCell = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
