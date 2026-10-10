package com.abctelecom.nms.model;

/** Application roles for RBAC (least privilege). */
public enum Role {
    NETWORK_ADMIN("Network Administrator"),
    NOC_ENGINEER("NOC Engineer"),
    OPTIMIZATION_ENGINEER("Optimization Engineer"),
    CLOUD_ADMIN("Cloud Administrator"),
    EDGE_ADMIN("Edge Administrator");

    private final String label;
    Role(String label) { this.label = label; }
    public String label() { return label; }

    public static Role fromString(String s) {
        for (Role r : values()) if (r.name().equalsIgnoreCase(s)) return r;
        throw new IllegalArgumentException("Unknown role: " + s);
    }
}
