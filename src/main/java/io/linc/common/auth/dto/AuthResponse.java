package io.linc.common.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String message;
    private String userName;

    public AuthResponse(String message, String userName) {
        this.message = message;
        this.userName = userName;
    }
}