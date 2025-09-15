package com.medgenome.auth.service;


import com.medgenome.auth.dto.AuthResponse;
import com.medgenome.auth.dto.SignUpRequest;
import com.medgenome.auth.entity.Tenant;
import com.medgenome.auth.entity.User;
import com.medgenome.auth.repository.TenantRepository;
import com.medgenome.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final VerificationService verificationService;


    @Transactional
    public AuthResponse signup(SignUpRequest request) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String hashedPassword = encoder.encode(request.getPasswordHash());

        // 3. Create new Tenant (minimal)
        Tenant tenant = new Tenant();
        tenant.setName("Tenant_" + UUID.randomUUID()); // todo - unique name to be used to differentiate tenants
        tenantRepository.save(tenant);

        // 4. Create new User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(hashedPassword);
        user.setUsername(request.getEmail());
        user.setIsActive(true);
        user.setMfaEnabled(true);
        user.setTenant(tenant);

        userRepository.save(user);

        //verificationService.initiateVerification(user);  todo -- verification of email and phone number entered for sign up

        return AuthResponse.builder()
                .message("Signup successful")
                .userName(user.getEmail())
                .build();
    }
}