package com.medgenome.auth.controller;

import com.medgenome.auth.dto.AuthResponse;
import com.medgenome.auth.dto.SignUpRequest;
import com.medgenome.auth.service.SignUpService;
import com.medgenome.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpService signUpService;

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignUpRequest request) {
        try {
            return userService.signUp(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthResponse(e.getMessage(), request.getEmail()));
        }
    }



}