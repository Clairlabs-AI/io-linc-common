package io.linc.auth.service;

public interface SmsService {
    void sendOtp(String phoneNumber, String otp);
}

