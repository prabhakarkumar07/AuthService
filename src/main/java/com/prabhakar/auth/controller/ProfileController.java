package com.prabhakar.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prabhakar.auth.dto.ApiResponse;
import com.prabhakar.auth.dto.ProfileResponse;
import com.prabhakar.auth.model.User;
import com.prabhakar.auth.model.UserProfile;
import com.prabhakar.auth.repository.UserRepository;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepo;

    public ProfileController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    
    @PutMapping("/update")
    public ApiResponse updateProfile(
            Authentication auth,
            @RequestBody UserProfile updated) {

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = user.getProfile();

        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        profile.setFullName(updated.getFullName());
        profile.setPhone(updated.getPhone());
        profile.setAddress(updated.getAddress());

        user.setProfile(profile);
        userRepo.save(user);

        ProfileResponse response = new ProfileResponse(
                profile.getId(),
                auth.getName(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getAddress()
        );

        return ApiResponse.success(response);
    }



    @GetMapping("/me")
    public ApiResponse getMyProfile(Authentication auth) {

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = user.getProfile();

        if (profile == null) {
            return ApiResponse.success("Profile not created yet");
        }

        ProfileResponse response = new ProfileResponse(
                profile.getId(),
                auth.getName(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getAddress()
        );

        return ApiResponse.success(response);
    }


}
