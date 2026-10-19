package com.muller_tomas.reading_groups.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailVerificationRequest {
    @NotBlank
    @Size(max = 128)
    private String token;

    public EmailVerificationRequest() {
    }

    public EmailVerificationRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
