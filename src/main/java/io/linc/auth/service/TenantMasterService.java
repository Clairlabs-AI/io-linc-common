package io.linc.auth.service;

import io.linc.auth.entity.TenantMaster;
import io.linc.auth.repository.TenantMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TenantMasterService {
    private final TenantMasterRepository tenantMasterRepository;

    @Transactional
    public TenantMaster createTenant(TenantMaster tenantMaster) {
        return tenantMasterRepository.save(tenantMaster);
    }

    @Transactional(readOnly = true)
    public Optional<TenantMaster> getTenantByCode(String tenantCode) {
        return tenantMasterRepository.findByTenantCode(tenantCode);
    }

    @Transactional(readOnly = true)
    public Optional<TenantMaster> getTenantByEmail(String emailId) {
        return tenantMasterRepository.findByEmailId(emailId);
    }

    @Transactional(readOnly = true)
    public Optional<Integer> getTenantIdByCode(String tenantCode) {
        return tenantMasterRepository.findByTenantCode(tenantCode)
                .map(TenantMaster::getTenantId);
    }

    @Transactional(readOnly = true)
    public Optional<Integer> getTenantIdByEmail(String emailId) {
        return tenantMasterRepository.findByEmailId(emailId)
                .map(TenantMaster::getTenantId);
    }

    @Transactional(readOnly = true)
    public Optional<TenantMaster> getFirstTenant() {
        return tenantMasterRepository.findById(1);
    }

    @Transactional(readOnly = true)
    public Optional<Integer> getFirstTenantId() {
        return tenantMasterRepository.findById(1).map(TenantMaster::getTenantId);
    }
}
