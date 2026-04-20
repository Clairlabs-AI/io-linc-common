package io.linc.common.auth.service;

import io.linc.common.auth.config.MultiTenantAuthProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
@ConditionalOnProperty(prefix = "auth.mail", name = "enabled", havingValue = "true", matchIfMissing = false)
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
