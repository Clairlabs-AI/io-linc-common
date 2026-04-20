package io.linc.common.auth.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SsoResponse {
    private String accessToken;
    private String refreshToken;
    private String sessionId;
    private String tokenType;
    private long expiresIn;
}
