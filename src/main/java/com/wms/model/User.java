package com.wms.model;

import java.sql.Timestamp;
import java.util.List;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String email;
    private String status;  
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private Timestamp lastLoginAt;
    private List<String> roles;

    public User() {}

    public int getUserId()             { return userId; }
    public void setUserId(int userId)  { this.userId = userId; }

    public String getUsername()              { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash()                    { return passwordHash; }
    public void setPasswordHash(String passwordHash)   { this.passwordHash = passwordHash; }

    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }

    public String getStatus()              { return status; }
    public void setStatus(String status)   { this.status = status; }

    public String getFullName()              { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber()               { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }

    public String getAvatarUrl()               { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Timestamp getLastLoginAt()                  { return lastLoginAt; }
    public void setLastLoginAt(Timestamp lastLoginAt)  { this.lastLoginAt = lastLoginAt; }

    public List<String> getRoles()             { return roles; }
    public void setRoles(List<String> roles)   { this.roles = roles; }

    // Kiểm tra có role nhất định không
    public boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }
}