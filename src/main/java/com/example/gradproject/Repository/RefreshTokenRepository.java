package com.example.gradproject.Repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.gradproject.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    // Cleanup expired tokens (scheduled task if needed)
    List<RefreshToken> findByExpiryDateBefore(Date date);
}
