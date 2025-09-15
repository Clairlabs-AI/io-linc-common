package com.medgenome.auth.dto;

import lombok.Data;

@Data
public class PhoneOtpRequest {
    private String phone;
    private String otp;
}

