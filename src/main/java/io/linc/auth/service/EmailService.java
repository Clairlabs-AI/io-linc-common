package io.linc.auth.service;

public interface EmailService {
    void send(String to, String subject, String content);
}
