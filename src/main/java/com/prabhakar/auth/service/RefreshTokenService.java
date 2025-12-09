package com.prabhakar.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prabhakar.auth.exception.TokenRefreshException;
import com.prabhakar.auth.model.RefreshToken;
import com.prabhakar.auth.model.User;
import com.prabhakar.auth.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    public RefreshToken createRefreshToken(User user,String deviceId) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setDeviceId(deviceId);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        rt.setRevoked(false);
        return repo.save(rt);
    }

    /**
     * Validate the refresh token and rotate it:
     * - If token not found or expired => throw
     * - If token is revoked (reuse detected) => revoke ALL tokens for this user and throw TokenRefreshException
     * - If valid => revoke this token, create a new refresh token, return the new token
     */
    @Transactional
    public RefreshToken validateAndRotate(String token) {
        RefreshToken rt = repo.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        // Reuse detection: if token was already revoked => possible theft / replay
        if (rt.isRevoked()) {
            // Revoke all tokens for this user immediately (security measure)
            revokeAllTokensForUser(rt.getUser());
            throw new TokenRefreshException("Detected refresh token reuse. All sessions revoked. Please login again.");
        }

        // Expiry check
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            // revoke this token and throw
            rt.setRevoked(true);
            repo.save(rt);
            throw new TokenRefreshException("Refresh token expired. Please login again.");
        }

        // Token is valid -> rotate: revoke old, create new, return new
        rt.setRevoked(true);
        repo.save(rt);

        RefreshToken newToken = createRefreshToken(rt.getUser(),rt.getDeviceId());
        return newToken;
    }

    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        repo.save(token);
    }
    
    @Transactional
    public void invalidateRefreshToken(String username, String deviceId) {
        repo.deleteByUserUsernameAndDeviceId(username, deviceId);
    }

    @Transactional
    public void invalidateAllRefreshTokens(String username) {
        repo.deleteByUserUsername(username);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }
    
    @Transactional
    public void revokeAllTokensForUser(User user) {
    	List<RefreshToken> tokens = repo.findByUser(user);

        for (RefreshToken t : tokens) {
            if (!t.isRevoked()) {
                t.setRevoked(true);
            }
        }
        repo.saveAll(tokens);
    }
}
