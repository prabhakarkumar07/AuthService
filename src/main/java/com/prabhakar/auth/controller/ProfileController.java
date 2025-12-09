package com.prabhakar.auth.controller;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prabhakar.auth.dto.ApiResponse;
import com.prabhakar.auth.dto.ProfileResponse;
import com.prabhakar.auth.model.User;
import com.prabhakar.auth.model.UserProfile;
import com.prabhakar.auth.repository.UserRepository;
import com.prabhakar.auth.service.CloudinaryService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepo;
    private final CloudinaryService cloudinaryService;

    public ProfileController(UserRepository userRepo,CloudinaryService cloudinaryService ) {
        this.userRepo = userRepo;
        this.cloudinaryService = cloudinaryService;
    }
    @CacheEvict(value = "profiles", key = "#auth.name")
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


    @Cacheable(value = "profiles", key = "#auth.name")
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
    
    
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse uploadProfile(
            Authentication auth,
            @RequestPart("file") MultipartFile file) {

        // 1️⃣ Validate file empty
        if (file.isEmpty()) {
            return ApiResponse.error(400, "INVALID_FILE", "File is empty");
        }

        // 2️⃣ Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            return ApiResponse.error(400, "INVALID_TYPE", "Only JPG & PNG allowed");
        }

        // 3️⃣ Validate extension
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
            return ApiResponse.error(400, "INVALID_EXTENSION", "Only JPG & PNG file extensions allowed");
        }

        // 4️⃣ Fetch user
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        // 5️⃣ Upload to Cloudinary
        String imageUrl = cloudinaryService.uploadProfile(file);

        // 6️⃣ Save in DB
        profile.setProfileImage(imageUrl);
        user.setProfile(profile);
        userRepo.save(user);

        return ApiResponse.success("Profile image updated: " + imageUrl);
    }

}
