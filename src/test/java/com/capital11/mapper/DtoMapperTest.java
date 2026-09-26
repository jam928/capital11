package com.capital11.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.capital11.domain.Account;
import com.capital11.domain.AccountType;
import com.capital11.domain.BankTransaction;
import com.capital11.domain.Customer;
import com.capital11.domain.TransactionType;
import com.capital11.dto.request.RegisterRequest;
import com.capital11.dto.request.RegistrationForm;
import com.capital11.dto.response.AccountResponse;
import com.capital11.dto.response.CustomerResponse;
import com.capital11.dto.response.TransactionResponse;

class DtoMapperTest {

    private final DtoMapper mapper = Mappers.getMapper(DtoMapper.class);

    @Test
    void mapsCustomerToResponse() {
        Customer customer = new Customer(4242, "Ann Lee", "ann@example.com", "ann", "secret", "3/14/1995", "f");

        assertThat(mapper.toResponse(customer))
                .isEqualTo(new CustomerResponse(4242, "Ann Lee", "ann@example.com", "ann", "3/14/1995", "f"));
    }

    @Test
    void mapsAccountTypeToTypeField() {
        Account account = Account.builder()
                .acctNum(7)
                .acctType(AccountType.COLLEGE)
                .balance(new BigDecimal("12.34"))
                .build();

        assertThat(mapper.toResponse(account))
                .isEqualTo(new AccountResponse(7, AccountType.COLLEGE, new BigDecimal("12.34")));
    }

    @Test
    void mapsTransactionsInOrder() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 26, 14, 30);
        BankTransaction deposit = BankTransaction.builder()
                .tid(1).type(TransactionType.DEPOSIT).amount(new BigDecimal("50.00")).date(now).build();
        BankTransaction withdrawal = BankTransaction.builder()
                .tid(2).type(TransactionType.WITHDRAWAL).amount(new BigDecimal("5.00")).date(now.minusDays(1)).build();

        assertThat(mapper.toTransactionResponses(List.of(deposit, withdrawal))).containsExactly(
                new TransactionResponse(1, TransactionType.DEPOSIT, new BigDecimal("50.00"), now),
                new TransactionResponse(2, TransactionType.WITHDRAWAL, new BigDecimal("5.00"), now.minusDays(1)));
    }

    @Test
    void unsavedEntitiesMapIdsToZero() {
        assertThat(mapper.toResponse(new Customer()).cid()).isZero();
        assertThat(mapper.toResponse(new Account()).acctNum()).isZero();
        assertThat(mapper.toResponse(new BankTransaction()).tid()).isZero();
    }

    @Test
    void nullsMapToNull() {
        assertThat(mapper.toResponse((Customer) null)).isNull();
        assertThat(mapper.toResponse((Account) null)).isNull();
        assertThat(mapper.toResponse((BankTransaction) null)).isNull();
        assertThat(mapper.toTransactionResponses(null)).isNull();
        assertThat(mapper.toCustomer((RegistrationForm) null)).isNull();
        assertThat(mapper.toCustomer((RegisterRequest) null)).isNull();
    }

    @Test
    void mapsRegistrationFormToCustomerWithLegacyBirthday() {
        RegistrationForm form = new RegistrationForm();
        form.setName("Ann Lee");
        form.setEmail("ann@example.com");
        form.setUsername("ann");
        form.setPassword("secret");
        form.setConfirmPassword("secret");
        form.setBirthMonth(3);
        form.setBirthDay(14);
        form.setBirthYear(1995);
        form.setGender("f");

        Customer customer = mapper.toCustomer(form);

        assertThat(customer.getCid()).isNull();
        assertThat(customer.getName()).isEqualTo("Ann Lee");
        assertThat(customer.getEmail()).isEqualTo("ann@example.com");
        assertThat(customer.getUsername()).isEqualTo("ann");
        assertThat(customer.getPassword()).isEqualTo("secret");
        assertThat(customer.getBirthday()).isEqualTo("3/14/1995");
        assertThat(customer.getGender()).isEqualTo("f");
    }

    @Test
    void mapsRegisterRequestToCustomerWithLegacyBirthday() {
        RegisterRequest request = new RegisterRequest("Ann Lee", "ann@example.com", "ann", "secret",
                LocalDate.of(1995, 3, 4), "f", AccountType.CHECKING);

        Customer customer = mapper.toCustomer(request);

        assertThat(customer.getCid()).isNull();
        assertThat(customer.getName()).isEqualTo("Ann Lee");
        assertThat(customer.getEmail()).isEqualTo("ann@example.com");
        assertThat(customer.getUsername()).isEqualTo("ann");
        assertThat(customer.getPassword()).isEqualTo("secret");
        // Not ISO (1995-03-04): the legacy column stores M/D/YYYY.
        assertThat(customer.getBirthday()).isEqualTo("3/4/1995");
        assertThat(customer.getGender()).isEqualTo("f");
    }
}
