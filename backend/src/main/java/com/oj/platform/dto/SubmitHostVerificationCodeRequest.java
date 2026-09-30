package com.oj.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmitHostVerificationCodeRequest {

    @NotBlank(message = "Verification code is required")
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
