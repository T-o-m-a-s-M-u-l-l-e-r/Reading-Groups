package com.muller_tomas.reading_groups.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.muller_tomas.reading_groups.dto.EmailVerificationRequest;
import com.muller_tomas.reading_groups.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Email verification")
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/api/auth/email-verification/resend")
    @Operation(summary = "Request another verification email", description = "Requires an access token; subject to a resend cooldown.")
    public ResponseEntity<Void> resend(@AuthenticationPrincipal Integer userId) {
        emailVerificationService.requestVerification(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/email-verification/verify")
    @Operation(summary = "Verify an email address", description = "Consumes the emailed verification token. No login required.")
    public ResponseEntity<Void> verify(@Valid @RequestBody EmailVerificationRequest request) {
        emailVerificationService.verify(request.getToken());
        return ResponseEntity.noContent().build();
    }
}
