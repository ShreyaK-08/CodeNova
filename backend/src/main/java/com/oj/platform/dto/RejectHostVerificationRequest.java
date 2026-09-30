package com.oj.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class RejectHostVerificationRequest {

    @NotBlank(message = "A rejection reason is required")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
