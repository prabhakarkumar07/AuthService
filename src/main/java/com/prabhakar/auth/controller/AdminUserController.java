package com.prabhakar.auth.controller;

import com.prabhakar.auth.repository.UserRepository;
import com.prabhakar.auth.dto.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepo;

    public AdminUserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // GET ALL USERS → requires USER_READ
    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success(userRepo.findAll())
        );
    }

    // DELETE USER → requires USER_DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepo.existsById(id)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(404,"USER_NOT_FOUND","User not found"));
        }
        userRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }
}
