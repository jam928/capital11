package com.capital11.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import com.capital11.domain.Account;
import com.capital11.domain.AccountType;
import com.capital11.domain.BankTransaction;
import com.capital11.domain.Customer;
import com.capital11.domain.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request and response bodies of the JSON API; entities are never serialized directly. */
final class ApiDtos {

    private ApiDtos() {
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(max = 50) String username,
            @NotBlank String password,
            @NotNull @Past LocalDate birthday,
            @NotBlank String gender,
            @NotNull AccountType accountType) {

        /** Same M/D/YYYY format the legacy app stored. */
        String legacyBirthday() {
            return birthday.getMonthValue() + "/" + birthday.getDayOfMonth() + "/" + birthday.getYear();
        }
    }

    record ProfileUpdateRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(max = 50) String username,
            @Schema(description = "Leave out or blank to keep the current password.") String password) {
    }

    record AmountRequest(@NotNull @Schema(example = "25.00") BigDecimal amount) {
    }

    record UsernameAvailability(String username, boolean available) {
    }

    record CustomerResponse(int cid, String name, String email, String username,
                            @Schema(description = "M/D/YYYY") String birthday, String gender) {

        static CustomerResponse of(Customer c) {
            return new CustomerResponse(c.getCid(), c.getName(), c.getEmail(), c.getUsername(),
                    c.getBirthday(), c.getGender());
        }
    }

    record AccountResponse(int acctNum, AccountType type, BigDecimal balance) {

        static AccountResponse of(Account a) {
            return new AccountResponse(a.getAcctNum(), a.getAcctType(), a.getBalance());
        }
    }

    record TransactionResponse(int tid, TransactionType type, BigDecimal amount, LocalDateTime date) {

        static TransactionResponse of(BankTransaction t) {
            return new TransactionResponse(t.getTid(), t.getType(), t.getAmount(), t.getDate());
        }
    }
}
