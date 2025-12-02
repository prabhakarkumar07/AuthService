package com.prabhakar.auth.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prabhakar.auth.dto.ApiResponse;
import com.prabhakar.auth.dto.PagedResponse;
import com.prabhakar.auth.dto.ProfileResponse;
import com.prabhakar.auth.model.User;
import com.prabhakar.auth.model.UserProfile;
import com.prabhakar.auth.repository.UserRepository;

@RestController
@RequestMapping("/auth/users")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public ApiResponse getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        String sortField = sort[0];
        String sortDir = sort[1];

        Sort sorting = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<User> result = userRepo.findAll(pageable);

        // Convert User → ProfileResponse
        List<ProfileResponse> profiles = result.getContent()
                .stream()
                .map(u -> {
                    UserProfile p = u.getProfile();
                    return new ProfileResponse(
                            u.getId(),
                            u.getUsername(),
                            p != null ? p.getFullName() : null,
                            p != null ? p.getPhone() : null,
                            p != null ? p.getAddress() : null
                    );
                })
                .toList();

        // Wrap inside paged response
        PagedResponse<ProfileResponse> response = new PagedResponse<>(
                profiles,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );

        return ApiResponse.success(response);
    }

}

