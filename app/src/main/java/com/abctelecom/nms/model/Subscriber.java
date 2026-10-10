package com.abctelecom.nms.model;

/** Subscriber entity. */
public class Subscriber {
    private String subscriberId;   // SUB-00001
    private String name;
    private String servicePlan;    // BASIC|STANDARD|PREMIUM|ENTERPRISE|IOT
    private int qosLevel;          // 1..5
    private String connectedCell;  // station id or null
    private int dataUsageMb;

    public String getSubscriberId() { return subscriberId; }
    public void setSubscriberId(String v) { this.subscriberId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getServicePlan() { return servicePlan; }
    public void setServicePlan(String v) { this.servicePlan = v; }
    public int getQosLevel() { return qosLevel; }
    public void setQosLevel(int v) { this.qosLevel = v; }
    public String getConnectedCell() { return connectedCell; }
    public void setConnectedCell(String v) { this.connectedCell = v; }
    public int getDataUsageMb() { return dataUsageMb; }
    public void setDataUsageMb(int v) { this.dataUsageMb = v; }
}
