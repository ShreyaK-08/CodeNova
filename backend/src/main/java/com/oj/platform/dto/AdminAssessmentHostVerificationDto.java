package com.oj.platform.dto;

import java.time.LocalDateTime;

/**
 * Admin's view of one user's verification request. Includes the user's identity
 * (name/username/email) and the document's original filename so the admin can decide
 * whether to approve/reject - but still never the on-disk path or the code hash. The
 * actual document bytes are retrieved separately via the dedicated, admin-only
 * download endpoint (never inlined as base64 here).
 */
public class AdminAssessmentHostVerificationDto {

    private Long id;
    private Long userId;
    private String userName;
    private String username;
    private String userEmail;
    private String status;
    private String documentOriginalName;
    private boolean documentAvailable;
    private String organizationName;
    private String organizationType;
    private String registrationNumber;
    private String officialEmail;
    private String country;
    private String website;
    private String representativeName;
    private String contactNumber;
    private String purpose;
    private String gstOriginalName;
    private boolean gstAvailable;
    private String authLetterOriginalName;
    private boolean authLetterAvailable;
    private String supportingDocOriginalName;
    private boolean supportingDocAvailable;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByUsername;
    private LocalDateTime verifiedAt;
    private String emailDeliveryStatus;
    private String emailDeliveryMessage;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public void setOfficialEmail(String officialEmail) {
        this.officialEmail = officialEmail;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getGstOriginalName() {
        return gstOriginalName;
    }

    public void setGstOriginalName(String gstOriginalName) {
        this.gstOriginalName = gstOriginalName;
    }

    public boolean isGstAvailable() {
        return gstAvailable;
    }

    public void setGstAvailable(boolean gstAvailable) {
        this.gstAvailable = gstAvailable;
    }

    public String getAuthLetterOriginalName() {
        return authLetterOriginalName;
    }

    public void setAuthLetterOriginalName(String authLetterOriginalName) {
        this.authLetterOriginalName = authLetterOriginalName;
    }

    public boolean isAuthLetterAvailable() {
        return authLetterAvailable;
    }

    public void setAuthLetterAvailable(boolean authLetterAvailable) {
        this.authLetterAvailable = authLetterAvailable;
    }

    public String getSupportingDocOriginalName() {
        return supportingDocOriginalName;
    }

    public void setSupportingDocOriginalName(String supportingDocOriginalName) {
        this.supportingDocOriginalName = supportingDocOriginalName;
    }

    public boolean isSupportingDocAvailable() {
        return supportingDocAvailable;
    }

    public void setSupportingDocAvailable(boolean supportingDocAvailable) {
        this.supportingDocAvailable = supportingDocAvailable;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentOriginalName() {
        return documentOriginalName;
    }

    public void setDocumentOriginalName(String documentOriginalName) {
        this.documentOriginalName = documentOriginalName;
    }

    public boolean isDocumentAvailable() {
        return documentAvailable;
    }

    public void setDocumentAvailable(boolean documentAvailable) {
        this.documentAvailable = documentAvailable;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewedByUsername() {
        return reviewedByUsername;
    }

    public void setReviewedByUsername(String reviewedByUsername) {
        this.reviewedByUsername = reviewedByUsername;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getEmailDeliveryStatus() {
        return emailDeliveryStatus;
    }

    public void setEmailDeliveryStatus(String emailDeliveryStatus) {
        this.emailDeliveryStatus = emailDeliveryStatus;
    }

    public String getEmailDeliveryMessage() {
        return emailDeliveryMessage;
    }

    public void setEmailDeliveryMessage(String emailDeliveryMessage) {
        this.emailDeliveryMessage = emailDeliveryMessage;
    }
}
