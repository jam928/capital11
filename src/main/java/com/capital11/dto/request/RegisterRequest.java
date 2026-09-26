package com.capital11.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import com.capital11.domain.AccountType;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(max = 50) String username,
        @NotBlank String password,
        @NotNull @Past LocalDate birthday,
        @NotBlank String gender,
        @NotNull AccountType accountType) {

    /** Same M/D/YYYY format the legacy app stored. */
    public String legacyBirthday() {
        return birthday.getMonthValue() + "/" + birthday.getDayOfMonth() + "/" + birthday.getYear();
    }
}
