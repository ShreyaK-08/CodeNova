package com.oj.platform.dto;

import java.time.LocalDateTime;

/**
 * What a normal user is allowed to see about their OWN verification request.
 * Deliberately excludes the document's stored path, the code hash, and anything
 * about other users - only status/timestamps/rejection reason.
 */
public class AssessmentHostVerificationDto {

    private Long id;
    private String status;
    private String documentOriginalName;
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
    private String authLetterOriginalName;
    private String supportingDocOriginalName;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime verifiedAt;
    /** True once an approval has generated a code and (attempted to) email it - lets
     *  the frontend show the "enter your code" form only when relevant. */
    private boolean codePending;
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

    public String getAuthLetterOriginalName() {
        return authLetterOriginalName;
    }

    public void setAuthLetterOriginalName(String authLetterOriginalName) {
        this.authLetterOriginalName = authLetterOriginalName;
    }

    public String getSupportingDocOriginalName() {
        return supportingDocOriginalName;
    }

    public void setSupportingDocOriginalName(String supportingDocOriginalName) {
        this.supportingDocOriginalName = supportingDocOriginalName;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public boolean isCodePending() {
        return codePending;
    }

    public void setCodePending(boolean codePending) {
        this.codePending = codePending;
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
