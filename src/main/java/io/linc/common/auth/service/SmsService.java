package io.linc.common.auth.service;

public interface SmsService {
    void sendOtp(String phoneNumber, String otp);
}

