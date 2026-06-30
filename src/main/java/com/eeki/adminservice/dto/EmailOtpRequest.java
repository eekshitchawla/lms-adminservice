package com.eeki.adminservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailOtpRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email
) {}
