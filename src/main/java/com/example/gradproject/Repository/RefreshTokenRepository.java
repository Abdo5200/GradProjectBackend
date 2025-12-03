package com.example.gradproject.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.gradproject.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    List<RefreshToken> findByUsername(String username);

    void deleteByUsername(String username);

    Optional<RefreshToken> findByUsernameAndDeviceId(String username, String deviceId);

    void deleteByUsernameAndDeviceId(String username, String deviceId);

    Optional<RefreshToken> findByDeviceId(String deviceId);

    void deleteByDeviceId(String deviceId);

    Optional<RefreshToken> findByAccessTokenHash(String accessTokenHash);

    List<RefreshToken> findByExpiryDateBefore(Date date);
}
