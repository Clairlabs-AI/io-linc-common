package com.medgenome.auth.service;

import com.medgenome.auth.config.MultiTenantAuthProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.text.MessageFormat;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final MultiTenantAuthProperties properties;

    public EmailServiceImpl(JavaMailSender mailSender, MultiTenantAuthProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMail().getFrom());
        message.setTo(to);
        message.setSubject(properties.getMail().getSubject());
        message.setText(MessageFormat.format(
                properties.getOtp().getEmailTemplate(),
                subject,
                properties.getOtp().getValidityMinutes()));
        mailSender.send(message);
    }
}
