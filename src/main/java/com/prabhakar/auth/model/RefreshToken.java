package com.prabhakar.auth.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    private Instant expiryDate;

    private boolean revoked = false;

    // relationship with User
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // getters & setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public Instant getExpiryDate() { return expiryDate; }
    public boolean isRevoked() { return revoked; }
    public User getUser() { return user; }

    public void setId(Long id) { this.id = id; }
    public void setToken(String token) { this.token = token; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public void setUser(User user) { this.user = user; }
}
