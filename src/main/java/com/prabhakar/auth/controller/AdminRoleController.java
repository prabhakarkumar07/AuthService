package com.prabhakar.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prabhakar.auth.cache.UserAuthCacheService;
import com.prabhakar.auth.dto.ApiResponse;
import com.prabhakar.auth.model.Role;
import com.prabhakar.auth.model.User;
import com.prabhakar.auth.repository.RoleRepository;
import com.prabhakar.auth.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    private final UserAuthCacheService cacheService;

    public AdminRoleController(UserRepository userRepo, RoleRepository roleRepo, UserAuthCacheService cacheService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.cacheService = cacheService;
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse updateUserRole(
            @PathVariable Long userId,
            @RequestParam String roleName) {

        // Only allow USER or ADMIN
        if (!roleName.equals("ROLE_USER") && !roleName.equals("ROLE_ADMIN")) {
            return ApiResponse.error(403,"PERMISSION_DENIED","Admins can assign only ROLE_USER or ROLE_ADMIN");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role newRole = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(newRole);
        userRepo.save(user);
        // 🔥 Clear Redis cache
        cacheService.deleteUserAuth(user.getUsername());

        return ApiResponse.success(
                "User role updated to " + newRole.getName()
        );
    }
    
    @PutMapping("/super/{userId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ApiResponse superAdminUpdateRole(
            @PathVariable Long userId,
            @RequestParam String roleName) {

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(role);
        userRepo.save(user);

        return ApiResponse.success("Role updated by superadmin");
    }

}
