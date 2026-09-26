package com.capital11.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.capital11.domain.AccountType;

class RegistrationFormTest {

    @Test
    void defaultsToCheckingAccount() {
        assertThat(new RegistrationForm().getAccountType()).isEqualTo(AccountType.CHECKING);
    }

    @Test
    void passwordIsConfirmedWhenBothMatch() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("secret");
        form.setConfirmPassword("secret");

        assertThat(form.isPasswordConfirmed()).isTrue();
    }

    @Test
    void passwordIsNotConfirmedWhenTheyDiffer() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("secret");
        form.setConfirmPassword("other");

        assertThat(form.isPasswordConfirmed()).isFalse();
    }

    @Test
    void missingPasswordIsLeftToNotBlankValidation() {
        assertThat(new RegistrationForm().isPasswordConfirmed()).isTrue();
    }

    @Test
    void birthdayUsesLegacyFormat() {
        RegistrationForm form = new RegistrationForm();
        form.setBirthMonth(12);
        form.setBirthDay(1);
        form.setBirthYear(2000);

        assertThat(form.birthday()).isEqualTo("12/1/2000");
    }
}
