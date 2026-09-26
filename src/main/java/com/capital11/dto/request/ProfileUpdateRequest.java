package com.capital11.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileUpdateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 50) String username,
        @Schema(description = "Leave out or blank to keep the current password.") String password) {
}
