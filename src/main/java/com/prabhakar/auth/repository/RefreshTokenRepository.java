package com.prabhakar.auth.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.prabhakar.auth.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserUsername(String username);

    Optional<RefreshToken> findByUserUsernameAndDeviceId(String username, String deviceId);

    void deleteByUserUsername(String username);

    void deleteByUserUsernameAndDeviceId(String username, String deviceId);

    // ⭐ FIXED: this replaces findAllByUser
    List<RefreshToken> findByUser(com.prabhakar.auth.model.User user);
}
