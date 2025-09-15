package com.medgenome.auth.repository;

import com.medgenome.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    // Or a combined query:
    Optional<User> findByEmailOrPhone(String email, String phone);

    boolean existsByEmail(@Email @NotBlank String email);

    boolean existsByPhone(@NotBlank String phone);

    Optional<User> findByEmailVerificationToken(String token);
}