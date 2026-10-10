package com.abctelecom.nms.model;

import java.time.LocalDateTime;

/** Resource allocation row (CPU/MEMORY/SPECTRUM/POWER/USER). */
public class ResourceAllocation {
    private int allocationId;
    private String resourceType;      // CPU|MEMORY|SPECTRUM|POWER|USER
    private String targetEntity;
    private double allocatedValue;
    private String unit;
    private double maxValue;
    private int allocatedBy;
    private LocalDateTime allocatedAt;

    public boolean isValidWithinCapacity() {
        return allocatedValue >= 0 && maxValue > 0 && allocatedValue <= maxValue;
    }

    public int getAllocationId() { return allocationId; }
    public void setAllocationId(int v) { this.allocationId = v; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String v) { this.resourceType = v; }
    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String v) { this.targetEntity = v; }
    public double getAllocatedValue() { return allocatedValue; }
    public void setAllocatedValue(double v) { this.allocatedValue = v; }
    public String getUnit() { return unit; }
    public void setUnit(String v) { this.unit = v; }
    public double getMaxValue() { return maxValue; }
    public void setMaxValue(double v) { this.maxValue = v; }
    public int getAllocatedBy() { return allocatedBy; }
    public void setAllocatedBy(int v) { this.allocatedBy = v; }
    public LocalDateTime getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(LocalDateTime v) { this.allocatedAt = v; }
}
