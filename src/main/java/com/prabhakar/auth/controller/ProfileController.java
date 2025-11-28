package com.prabhakar.auth.controller;

import com.prabhakar.auth.model.User;
import com.prabhakar.auth.repository.UserRepository;
import com.prabhakar.auth.dto.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepo;

    public ProfileController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/me")
    public ApiResponse getMyProfile(Authentication auth) {
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ApiResponse.success(user);
    }
}
