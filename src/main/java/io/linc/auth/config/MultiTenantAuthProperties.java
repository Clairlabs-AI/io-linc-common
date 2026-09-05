package io.linc.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth")
public class MultiTenantAuthProperties {
    private final Jwt jwt = new Jwt();
    private final Otp otp = new Otp();
    private final Twilio twilio = new Twilio();
    private final Mail mail = new Mail();

    public Otp getOtp() { return otp; }
    public Mail getMail() { return mail; }

    @Data
    public static class Jwt {
        private long accessTokenValidityMinutes = 15;
        private long refreshTokenValidityHours = 8;
        private String issuer = "multi-tenant-auth";
    }

    @Data
    public static class Otp {
        private int length = 6;
        private int validityMinutes = 5;
        private String emailTemplate = "Your OTP is: {0}. Valid for {1} minutes.";
        private String smsTemplate = "Your OTP is: {0}. Valid for {1} minutes.";
        public String getEmailTemplate() { return emailTemplate; }
        public int getValidityMinutes() { return validityMinutes; }
    }

    @Data
    public static class Twilio {
        private String accountSid;
        private String authToken;
        private String fromNumber;
    }

    @Data
    public static class Mail {
        private String from;
        private String subject = "Your OTP Code";
        public String getSubject() { return subject; }
        public String getFrom() { return from; }
    }
}