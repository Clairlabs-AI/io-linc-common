package com.medgenome.auth.service;

import com.medgenome.auth.config.MultiTenantAuthProperties;
import com.medgenome.auth.entity.*;
import com.medgenome.auth.repository.OtpRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final MultiTenantAuthProperties properties;
    private final JavaMailSender mailSender;
    private final Random random = new Random();

    @Transactional
    public void sendEmailOtp(String email, Integer tenantId) {
        String otp = generateOtp();
        saveOtp(otp, email, tenantId, UserOtp.OtpType.EMAIL);
        sendEmail(email, otp);
    }

    @Transactional
    public void sendSmsOtp(String phoneNumber, Integer tenantId) {
        String otp = generateOtp();
        saveOtp(otp, phoneNumber, tenantId, UserOtp.OtpType.SMS);
        sendSms(phoneNumber, otp);
    }

    @Transactional
    public boolean verifyOtp(String code, String identifier, Integer tenantId) {
        return otpRepository
                .findByCodeAndIdentifierAndTenantIdAndUsedFalse(code, identifier, tenantId)
                .filter(userOtp -> !userOtp.getExpiryDate().isBefore(Instant.now()))
                .map(userOtp -> {
                    userOtp.setUsed(true);
                    otpRepository.save(userOtp);
                    return true;
                })
                .orElse(false);
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < properties.getOtp().getLength(); i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private void saveOtp(String code, String identifier, Integer tenantId, UserOtp.OtpType type) {
        UserOtp userOtp = new UserOtp();
        userOtp.setCode(code);
        userOtp.setIdentifier(identifier);
        userOtp.setTenantId(tenantId);
        userOtp.setType(type);
        userOtp.setExpiryDate(Instant.now()
                .plusSeconds(properties.getOtp().getValidityMinutes() * 60L));
        otpRepository.save(userOtp);
    }

    private void sendEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMail().getFrom());
        message.setTo(email);
        message.setSubject(properties.getMail().getSubject());
        message.setText(MessageFormat.format(
                properties.getOtp().getEmailTemplate(),
                otp,
                properties.getOtp().getValidityMinutes()));
        mailSender.send(message);
    }

    private void sendSms(String phoneNumber, String otp) {
        Twilio.init(properties.getTwilio().getAccountSid(), 
                   properties.getTwilio().getAuthToken());
        
        Message.creator(
            new PhoneNumber(phoneNumber),
            new PhoneNumber(properties.getTwilio().getFromNumber()),
            MessageFormat.format(
                properties.getOtp().getSmsTemplate(),
                otp,
                properties.getOtp().getValidityMinutes())
        ).create();
    }

    @Transactional(readOnly = true)
    public List<String> getRolesByUserAndTenant(Long userId, Integer tenantId) {
        return otpRepository.findRolesByUserAndTenant(userId, tenantId)
                .stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getApplicationsByTenant(Integer tenantId) {
        return otpRepository.findApplicationsByTenant(tenantId)
                .stream()
                .map(Application::getName)
                .toList();
    }

    public List<String> getDomainsByTenant(Integer tenantId) {
        return otpRepository.findDomainsByTenant(tenantId)
                .stream()
                .map(Domain::getName)
                .toList();
    }
}