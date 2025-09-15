package com.medgenome.auth.controller;


import com.medgenome.auth.dto.SsoLoginRequest;
import com.medgenome.auth.dto.SsoResponse;
import com.medgenome.auth.service.SsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/sso")
@RequiredArgsConstructor
public class SsoController {
    private final SsoService ssoService;

    @PostMapping("/login")
    public ResponseEntity<SsoResponse> ssoLogin(@Valid @RequestBody SsoLoginRequest request) {
        return ResponseEntity.ok(ssoService.login(request));
    }

    @PostMapping("/login-tenant-master")
    public ResponseEntity<SsoResponse> ssoLoginTenantMaster() {
        return ResponseEntity.ok(ssoService.loginWithTenantMaster());
    }

    @PostMapping("/refresh")
    public ResponseEntity<SsoResponse> refresh(
            @RequestParam String sessionId,
            @RequestParam String refreshToken) {
        return ResponseEntity.ok(ssoService.refresh(sessionId, refreshToken));
    }

    @PostMapping("/validate/{sessionId}")
    public ResponseEntity<Void> validateSession(
            @PathVariable String sessionId,
            @RequestHeader("X-Tenant-ID") Integer tenantId) {
        ssoService.validateSession(sessionId, tenantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout/{sessionId}")
    public ResponseEntity<Void> logout(@PathVariable String sessionId) {
        ssoService.logout(sessionId);
        return ResponseEntity.ok().build();
    }
}
