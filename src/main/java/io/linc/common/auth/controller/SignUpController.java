package io.linc.common.auth.controller;

import io.linc.common.auth.dto.AuthResponse;
import io.linc.common.auth.dto.SignUpRequest;
import io.linc.common.auth.service.SignUpService;
import io.linc.common.auth.service.UserService;
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