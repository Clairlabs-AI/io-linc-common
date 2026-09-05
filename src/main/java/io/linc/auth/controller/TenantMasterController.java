package io.linc.auth.controller;

import io.linc.auth.entity.TenantMaster;
import io.linc.auth.service.TenantMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant-master")
@RequiredArgsConstructor
public class TenantMasterController {
    private final TenantMasterService tenantMasterService;

    @PostMapping("/create")
    public ResponseEntity<TenantMaster> createTenant(@RequestBody TenantMaster tenantMaster) {
        TenantMaster savedTenant = tenantMasterService.createTenant(tenantMaster);
        return ResponseEntity.ok(savedTenant);
    }
}

