package io.linc.auth.service;


import io.linc.auth.entity.TenantMaster;
import io.linc.auth.repository.TenantMasterRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class RefIdService {

    private final TenantMasterRepository tenantMasterRepository;

    public RefIdService(TenantMasterRepository tenantMasterRepository) {
        this.tenantMasterRepository = tenantMasterRepository;
    }

    /**
     * Generates an ID in the format <tenantCode>-<5charAlphanumeric>-<suffix> using tenantId
     * Looks up tenantCode from tenantmaster table
     */
    public String generateCustomIdFromTenantId(Integer tenantId, char suffix) {
        TenantMaster tenantMaster = tenantMasterRepository.findByTenantId(tenantId);
        if (tenantMaster == null || tenantMaster.getTenantCode() == null) {
            throw new IllegalArgumentException("Tenant code not found for tenantId: " + tenantId);
        }
        String tenantCode = tenantMaster.getTenantCode().length() > 3? tenantMaster.getTenantCode().substring(0, 3) : tenantMaster.getTenantCode();
        return generateCustomId(tenantCode, suffix);
    }

    public String generateCustomId(String tenantCode, char suffix) {
        String alphanumeric = generateRandomAlphanumeric();
        return tenantCode + "-" + alphanumeric + "-" + suffix;
    }


    /**
     * Generates a random 5-character alphanumeric string in the pattern digit+letter+digit+letter+digit.
     * Example: 3A7B2     *
     * @return A 5-character string alternating digit and letter
     */
    private String generateRandomAlphanumeric() {
        final String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String digits = "0123456789";
        char[] result = new char[5];
        result[0] = letters.charAt(ThreadLocalRandom.current().nextInt(letters.length()));
        result[1] = digits.charAt(ThreadLocalRandom.current().nextInt(digits.length()));
        result[2] = letters.charAt(ThreadLocalRandom.current().nextInt(letters.length()));
        result[3] = digits.charAt(ThreadLocalRandom.current().nextInt(digits.length()));
        result[4] = letters.charAt(ThreadLocalRandom.current().nextInt(letters.length()));
        return new String(result);
    }



}
