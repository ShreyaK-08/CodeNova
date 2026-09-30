package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tracks one user's progress through the "Host Assessment" verification workflow
 * (see AssessmentHostVerificationService). One row per user - resubmitting after a
 * REJECTED status reuses and updates the same row rather than creating a new one, so
 * there is always exactly one current status per user to look up.
 *
 * Security-sensitive fields are never serialized directly to JSON (JsonIgnore) - all
 * API responses go through AssessmentHostVerificationDto /
 * AdminAssessmentHostVerificationDto instead, which deliberately omit the raw code
 * hash and the on-disk file path.
 */
@Entity
@Table(name = "assessment_host_verifications")
public class AssessmentHostVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"password", "email"})
    private User user;

    /** Server-generated safe filename the document is actually stored under on disk
     *  (never derived from user input) - see FileStorageService. Never exposed via API. */
    @JsonIgnore
    @Column(name = "document_stored_name", length = 255)
    private String documentStoredName;

    /** Original filename as uploaded, kept only for display purposes (e.g. in the
     *  admin review table) - never used to build a filesystem path. */
    @Column(name = "document_original_name", length = 255)
    private String documentOriginalName;

    @Column(name = "document_content_type", length = 100)
    private String documentContentType;

    // Business / Organization Verification fields
    @Column(name = "organization_name", length = 255)
    private String organizationName;

    @Column(name = "organization_type", length = 100)
    private String organizationType;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "official_email", length = 150)
    private String officialEmail;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "representative_name", length = 150)
    private String representativeName;

    @Column(name = "contact_number", length = 50)
    private String contactNumber;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    // Optional document: GST Certificate
    @JsonIgnore
    @Column(name = "gst_stored_name", length = 255)
    private String gstStoredName;

    @Column(name = "gst_original_name", length = 255)
    private String gstOriginalName;

    @Column(name = "gst_content_type", length = 100)
    private String gstContentType;

    // Optional document: Authorization Letter / Representative Proof
    @JsonIgnore
    @Column(name = "auth_letter_stored_name", length = 255)
    private String authLetterStoredName;

    @Column(name = "auth_letter_original_name", length = 255)
    private String authLetterOriginalName;

    @Column(name = "auth_letter_content_type", length = 100)
    private String authLetterContentType;

    // Optional document: Additional Supporting Document
    @JsonIgnore
    @Column(name = "supporting_doc_stored_name", length = 255)
    private String supportingDocStoredName;

    @Column(name = "supporting_doc_original_name", length = 255)
    private String supportingDocOriginalName;

    @Column(name = "supporting_doc_content_type", length = 100)
    private String supportingDocContentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HostVerificationStatus status = HostVerificationStatus.PENDING;

    /** BCrypt hash of the one-time verification code - the plain code is never
     *  persisted anywhere (requirement: "DO NOT store plain verification code"). */
    @JsonIgnore
    @Column(name = "verification_code_hash", length = 255)
    private String verificationCodeHash;

    @Column(name = "code_expires_at")
    private LocalDateTime codeExpiresAt;

    /** Set true the instant a code is successfully consumed, so it can never be
     *  replayed even if it is still technically within its expiry window. */
    @Column(name = "code_used", nullable = false)
    private boolean codeUsed = false;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    @JsonIgnoreProperties({"password", "email"})
    private User reviewedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AssessmentHostVerification() {
    }

    public AssessmentHostVerification(User user) {
        this.user = user;
        this.status = HostVerificationStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDocumentStoredName() {
        return documentStoredName;
    }

    public void setDocumentStoredName(String documentStoredName) {
        this.documentStoredName = documentStoredName;
    }

    public String getDocumentOriginalName() {
        return documentOriginalName;
    }

    public void setDocumentOriginalName(String documentOriginalName) {
        this.documentOriginalName = documentOriginalName;
    }

    public String getDocumentContentType() {
        return documentContentType;
    }

    public void setDocumentContentType(String documentContentType) {
        this.documentContentType = documentContentType;
    }

    public HostVerificationStatus getStatus() {
        return status;
    }

    public void setStatus(HostVerificationStatus status) {
        this.status = status;
    }

    public String getVerificationCodeHash() {
        return verificationCodeHash;
    }

    public void setVerificationCodeHash(String verificationCodeHash) {
        this.verificationCodeHash = verificationCodeHash;
    }

    public LocalDateTime getCodeExpiresAt() {
        return codeExpiresAt;
    }

    public void setCodeExpiresAt(LocalDateTime codeExpiresAt) {
        this.codeExpiresAt = codeExpiresAt;
    }

    public boolean isCodeUsed() {
        return codeUsed;
    }

    public void setCodeUsed(boolean codeUsed) {
        this.codeUsed = codeUsed;
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

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

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

    public String getGstStoredName() {
        return gstStoredName;
    }

    public void setGstStoredName(String gstStoredName) {
        this.gstStoredName = gstStoredName;
    }

    public String getGstOriginalName() {
        return gstOriginalName;
    }

    public void setGstOriginalName(String gstOriginalName) {
        this.gstOriginalName = gstOriginalName;
    }

    public String getGstContentType() {
        return gstContentType;
    }

    public void setGstContentType(String gstContentType) {
        this.gstContentType = gstContentType;
    }

    public String getAuthLetterStoredName() {
        return authLetterStoredName;
    }

    public void setAuthLetterStoredName(String authLetterStoredName) {
        this.authLetterStoredName = authLetterStoredName;
    }

    public String getAuthLetterOriginalName() {
        return authLetterOriginalName;
    }

    public void setAuthLetterOriginalName(String authLetterOriginalName) {
        this.authLetterOriginalName = authLetterOriginalName;
    }

    public String getAuthLetterContentType() {
        return authLetterContentType;
    }

    public void setAuthLetterContentType(String authLetterContentType) {
        this.authLetterContentType = authLetterContentType;
    }

    public String getSupportingDocStoredName() {
        return supportingDocStoredName;
    }

    public void setSupportingDocStoredName(String supportingDocStoredName) {
        this.supportingDocStoredName = supportingDocStoredName;
    }

    public String getSupportingDocOriginalName() {
        return supportingDocOriginalName;
    }

    public void setSupportingDocOriginalName(String supportingDocOriginalName) {
        this.supportingDocOriginalName = supportingDocOriginalName;
    }

    public String getSupportingDocContentType() {
        return supportingDocContentType;
    }

    public void setSupportingDocContentType(String supportingDocContentType) {
        this.supportingDocContentType = supportingDocContentType;
    }
}
