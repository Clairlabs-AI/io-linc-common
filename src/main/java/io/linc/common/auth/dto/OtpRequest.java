package io.linc.common.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {
    @NotBlank
    private String identifier; // email or phone number
    
    @NotBlank
    private Integer tenantId;
}