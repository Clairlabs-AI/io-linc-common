package io.linc.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerificationRequest {
    @NotBlank
    private String code;
    
    @NotBlank
    private String identifier;
    
    @NotBlank
    private Integer tenantId;
}