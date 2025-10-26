package com.example.gradproject.Repository;

import com.example.gradproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String resetToken);
}
