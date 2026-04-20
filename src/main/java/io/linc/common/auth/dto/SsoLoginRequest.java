package io.linc.common.auth.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SsoLoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String tenantCode;

    /*@NotBlank
    private String tenantId;

    @NotBlank
    private String clientId;*/
}