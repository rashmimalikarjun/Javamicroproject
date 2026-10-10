package com.abctelecom.nms.model;

import java.time.LocalDateTime;

/** User entity. passwordHash uses PBKDF2 format; never expose it in UI/logs. */
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role role;
    private boolean enabled = true;
    private LocalDateTime createdAt;

    public User() {}
    public User(int userId, String username, String fullName, Role role) {
        this.userId = userId; this.username = username; this.fullName = fullName; this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int v) { this.userId = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public Role getRole() { return role; }
    public void setRole(Role v) { this.role = v; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
