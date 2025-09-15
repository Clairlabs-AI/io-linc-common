package com.medgenome.auth.service;

public interface SmsService {
    void sendOtp(String phoneNumber, String otp);
}

