package com.hydroplant.model;

/**
 * Simple user account used for the login dialog. Password storage here is
 * simplified for a student project (SHA-256 hash, no salt) — see README
 * for notes on hardening this for production use.
 */
public class User {
    public enum Role { ADMIN, OPERATOR }

    private int id;
    private String username;
    private String passwordHash;
    private Role role;

    public User(int id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
}
