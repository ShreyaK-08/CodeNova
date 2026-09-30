package com.oj.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload containing verified business/organization details for assessment host verification.
 */
public class SubmitHostBusinessVerificationRequest {

    @NotBlank(message = "Legal Organization / Company Name is required")
    @Size(max = 255)
    private String organizationName;

    @NotBlank(message = "Organization Type is required")
    @Size(max = 100)
    private String organizationType;

    @NotBlank(message = "Business / Company Registration Number is required")
    @Size(max = 100)
    private String registrationNumber;

    @NotBlank(message = "Official Organization Email is required")
    @Email(message = "Please provide a valid official email address")
    @Size(max = 150)
    private String officialEmail;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @NotBlank(message = "Organization Website is required")
    @Size(max = 255)
    private String website;

    @NotBlank(message = "Authorized Representative Name is required")
    @Size(max = 150)
    private String representativeName;

    @NotBlank(message = "Contact Number is required")
    @Size(max = 50)
    private String contactNumber;

    @NotBlank(message = "Purpose / Reason for Hosting Assessments is required")
    private String purpose;

    public SubmitHostBusinessVerificationRequest() {
    }

    public SubmitHostBusinessVerificationRequest(String organizationName, String organizationType, String registrationNumber,
                                                String officialEmail, String country, String website,
                                                String representativeName, String contactNumber, String purpose) {
        this.organizationName = organizationName;
        this.organizationType = organizationType;
        this.registrationNumber = registrationNumber;
        this.officialEmail = officialEmail;
        this.country = country;
        this.website = website;
        this.representativeName = representativeName;
        this.contactNumber = contactNumber;
        this.purpose = purpose;
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
}
