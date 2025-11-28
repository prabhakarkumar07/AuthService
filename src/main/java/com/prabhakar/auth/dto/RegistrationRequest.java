package com.prabhakar.auth.dto;

import jakarta.validation.constraints.*;

public class RegistrationRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username length must be between 3 and 50")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 100, message = "password must be at least 6 characters")
    private String password;

    @NotBlank(message = "role is required")
    private String role; // e.g. ROLE_USER

    // getters / setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
