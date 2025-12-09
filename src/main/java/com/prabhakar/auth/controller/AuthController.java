package com.prabhakar.auth.controller;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prabhakar.auth.dto.ApiResponse;
import com.prabhakar.auth.dto.RegistrationRequest;
import com.prabhakar.auth.dto.UserResponse;
import com.prabhakar.auth.exception.TokenRefreshException;
import com.prabhakar.auth.model.RefreshToken;
import com.prabhakar.auth.model.Role;
import com.prabhakar.auth.model.User;
import com.prabhakar.auth.repository.RoleRepository;
import com.prabhakar.auth.repository.UserRepository;
import com.prabhakar.auth.security.JwtUtil;
import com.prabhakar.auth.service.BlacklistService;
import com.prabhakar.auth.service.RefreshTokenService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;
    private final CacheManager cacheManager;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepo,
                          PasswordEncoder passwordEncoder,
                          RefreshTokenService refreshTokenService,
                          RoleRepository roleRepo,
                          BlacklistService blacklistService,
                          CacheManager cacheManager) {

        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.roleRepo = roleRepo;
        this.blacklistService = blacklistService;
        this.cacheManager = cacheManager;
    }

    // Create a default USER role user at startup
    @PostConstruct
    public void init() {
        if (userRepo.findByUsername("user").isEmpty()) {

            Role defaultRole = roleRepo.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found in DB"));

            User u = new User();
            u.setUsername("user");
            u.setPassword(passwordEncoder.encode("pass"));
            u.setRole(defaultRole);

            userRepo.save(u);

            log.info("Default test user created: username='user', password='pass'");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");
        String deviceId = body.get("deviceid");

        log.info("Login attempt: {}", username);

        authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Login successful for user: {} (role={})", username, user.getRole().getName());

        String accessToken = jwtUtil.generateToken(username,deviceId);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user,deviceId);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        )));
    }
    
    @PostMapping("/logout")
    public ApiResponse logout(HttpServletRequest request, Authentication auth) {

        String accessToken = jwtUtil.extractTokenFromRequest(request);
        if (accessToken == null) {
            return ApiResponse.error(400, "NO_TOKEN", "Access token missing");
        }

        String deviceId = jwtUtil.extractDeviceId(accessToken);
        long expiresIn = jwtUtil.getExpirationSeconds(accessToken);

        // 1) blacklist access token
        blacklistService.blacklistToken(accessToken, expiresIn);

        // figure username safely
        String username;
        if (auth != null && auth.getName() != null) {
            username = auth.getName();
        } else if (jwtUtil.validateToken(accessToken)) {
            username = jwtUtil.extractUsername(accessToken);
        } else {
            return ApiResponse.error(401, "UNAUTHENTICATED", "Authentication required for logout");
        }

        // 2) remove only current device refresh token
        refreshTokenService.invalidateRefreshToken(username, deviceId);

        // 3) clear cache
        if (cacheManager.getCache("profiles") != null) cacheManager.getCache("profiles").evict(username);
        if (cacheManager.getCache("users") != null) cacheManager.getCache("users").evict(username);

        return ApiResponse.success("Logged out from this device");
    }


    @PostMapping("/logout/all")
    public ApiResponse logoutAll(HttpServletRequest request, Authentication auth) {

        String token = jwtUtil.extractTokenFromRequest(request);
        if (token != null) {
            long exp = jwtUtil.getExpirationSeconds(token);
            blacklistService.blacklistToken(token, exp);
        }

        // 1️⃣ Remove ALL refresh tokens for this user
        refreshTokenService.invalidateAllRefreshTokens(auth.getName());

        // 2️⃣ Clear cache
        cacheManager.getCache("profiles").evict(auth.getName());
        cacheManager.getCache("users").evict(auth.getName());

        return ApiResponse.success("Logged out from all devices");
    }



    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest req) {

        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            log.warn("Registration failed: username '{}' already exists", req.getUsername());
            return ResponseEntity.badRequest().body(Map.of("error", "username already exists"));
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        // 👉 ALWAYS assign ROLE_USER for public registration
        Role defaultRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER missing in DB"));
        user.setRole(defaultRole);

        User saved = userRepo.save(user);

        log.info("New user registered: {}", saved.getUsername());

        UserResponse resp = new UserResponse(saved.getId(), saved.getUsername(), saved.getRole().getName());

        return ResponseEntity.created(URI.create("/auth/users/" + saved.getId()))
                .body(ApiResponse.success(resp));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {

        String refreshTokenStr = body.get("refreshToken");
        log.info("Refresh token request received");

        try {
            RefreshToken newRefreshToken = refreshTokenService.validateAndRotate(refreshTokenStr);

            String newAccessToken = jwtUtil.generateToken(newRefreshToken.getUser().getUsername(),newRefreshToken.getDeviceId());
            log.info("Refresh token rotated for user: {}", newRefreshToken.getUser().getUsername());

            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken.getToken()
            )));

        } catch (TokenRefreshException ex) {
            log.warn("Invalid refresh token attempt");
            throw ex;
        }
    }
}
