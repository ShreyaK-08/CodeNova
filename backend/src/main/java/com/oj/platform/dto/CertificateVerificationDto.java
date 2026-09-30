package com.oj.platform.dto;

import java.time.LocalDateTime;

/** Deliberately minimal - only what's needed to prove a certificate is real. No user id,
 *  email, or any other account details are exposed here. */
public class CertificateVerificationDto {
    private boolean verified;
    private String recipientName;
    private String title;
    private String achievementLevel;
    private String certificateNumber;
    private LocalDateTime issuedAt;
    private String verificationCode;
    private String message;

    public static CertificateVerificationDto notFound(String code) {
        CertificateVerificationDto dto = new CertificateVerificationDto();
        dto.verified = false;
        dto.verificationCode = code;
        dto.message = "No certificate found for this verification code.";
        return dto;
    }

    public static CertificateVerificationDto found(String recipientName, String title, String achievementLevel,
                                                     String certificateNumber, LocalDateTime issuedAt, String code) {
        CertificateVerificationDto dto = new CertificateVerificationDto();
        dto.verified = true;
        dto.recipientName = recipientName;
        dto.title = title;
        dto.achievementLevel = achievementLevel;
        dto.certificateNumber = certificateNumber;
        dto.issuedAt = issuedAt;
        dto.verificationCode = code;
        dto.message = "Certificate verified.";
        return dto;
    }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAchievementLevel() { return achievementLevel; }
    public void setAchievementLevel(String achievementLevel) { this.achievementLevel = achievementLevel; }
    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
