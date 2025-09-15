package com.medgenome.auth.service;

public interface EmailService {
    void send(String to, String subject, String content);
}
