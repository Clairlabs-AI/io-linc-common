package com.medgenome.auth.service;


import com.medgenome.auth.controller.VerificationController;
import com.medgenome.auth.dto.AuthResponse;
import com.medgenome.auth.dto.SignUpRequest;
import com.medgenome.auth.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SignUpService signUpService;
    private final VerificationService verificationService;


    public boolean existsByEmailOrPhone(String email, String phone) {
        return userRepository.findByEmailOrPhone(email, phone).isPresent();
    }

    public ResponseEntity<AuthResponse> signUp(SignUpRequest signUpRequest) {
        if (existsByEmailOrPhone(signUpRequest.getEmail(), signUpRequest.getPhone())) {
            throw new IllegalArgumentException("User with given email or phone number already exists");
        }
        return new ResponseEntity<>(signUpService.signup(signUpRequest), HttpStatus.OK);
    }


}
