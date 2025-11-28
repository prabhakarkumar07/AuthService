package com.prabhakar.auth.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/dashboard")
    public String userDashboard() {
        return "User dashboard: visible to USER and ADMIN";
    }
}
