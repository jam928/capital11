package com.capital11.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capital11.domain.Account;
import com.capital11.domain.AccountType;
import com.capital11.domain.BankTransaction;
import com.capital11.domain.Customer;
import com.capital11.domain.TransactionType;
import com.capital11.repository.AccountRepository;
import com.capital11.repository.BankTransactionRepository;
import com.capital11.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    private static final int CID = 4242;

    @Mock
    private CustomerRepository customers;

    @Mock
    private AccountRepository accounts;

    @Mock
    private BankTransactionRepository transactions;

    @InjectMocks
    private BankService bankService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Captor
    private ArgumentCaptor<BankTransaction> transactionCaptor;

    // ---------- register ----------

    @Test
    void registerAssignsFreeCidAndOpensAccountWithZeroBalance() {
        Customer newCustomer = customer("ann");
        newCustomer.setCid(null);
        when(customers.existsByUsername("ann")).thenReturn(false);
        // First random id is taken, the second is free.
        when(customers.existsById(anyInt())).thenReturn(true, false);
        when(customers.save(newCustomer)).thenReturn(newCustomer);

        Customer saved = bankService.register(newCustomer, AccountType.SAVINGS);

        assertThat(saved).isSameAs(newCustomer);
        assertThat(saved.getCid()).isBetween(1000, 99999);
        verify(accounts).save(accountCaptor.capture());
        Account account = accountCaptor.getValue();
        assertThat(account.getAcctType()).isEqualTo(AccountType.SAVINGS);
        assertThat(account.getCustomer()).isSameAs(newCustomer);
        assertThat(account.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    void registerRejectsTakenUsername() {
        when(customers.existsByUsername("ann")).thenReturn(true);

        assertThatThrownBy(() -> bankService.register(customer("ann"), AccountType.CHECKING))
                .isInstanceOf(BankException.class)
                .hasMessage("Username is already taken.");
        verify(customers, never()).save(any());
        verifyNoInteractions(accounts);
    }

    // ---------- lookups ----------

    @Test
    void authenticateReturnsCustomerWhenPasswordMatches() {
        Customer ann = customer("ann");
        when(customers.findByUsername("ann")).thenReturn(Optional.of(ann));

        assertThat(bankService.authenticate("ann", "secret")).contains(ann);
    }

    @Test
    void authenticateIsEmptyWhenPasswordIsWrong() {
        when(customers.findByUsername("ann")).thenReturn(Optional.of(customer("ann")));

        assertThat(bankService.authenticate("ann", "wrong")).isEmpty();
    }

    @Test
    void authenticateIsEmptyForUnknownUser() {
        when(customers.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThat(bankService.authenticate("nobody", "secret")).isEmpty();
    }

    @Test
    void isUsernameAvailableIsTheInverseOfExists() {
        when(customers.existsByUsername("taken")).thenReturn(true);
        when(customers.existsByUsername("free")).thenReturn(false);

        assertThat(bankService.isUsernameAvailable("taken")).isFalse();
        assertThat(bankService.isUsernameAvailable("free")).isTrue();
    }

    @Test
    void getCustomerReturnsCustomer() {
        Customer ann = customer("ann");
        when(customers.findById(CID)).thenReturn(Optional.of(ann));

        assertThat(bankService.getCustomer(CID)).isSameAs(ann);
    }

    @Test
    void getCustomerThrowsWhenMissing() {
        when(customers.findById(CID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.getCustomer(CID))
                .isInstanceOf(BankException.class)
                .hasMessage("Customer not found.");
    }

    @Test
    void findCustomerDelegatesToRepository() {
        Customer ann = customer("ann");
        when(customers.findByUsername("ann")).thenReturn(Optional.of(ann));

        assertThat(bankService.findCustomer("ann")).contains(ann);
    }

    @Test
    void getAccountReturnsAccount() {
        Account account = account("10.00");
        when(accounts.findByCustomerCid(CID)).thenReturn(Optional.of(account));

        assertThat(bankService.getAccount(CID)).isSameAs(account);
    }

    @Test
    void getAccountThrowsWhenMissing() {
        when(accounts.findByCustomerCid(CID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.getAccount(CID))
                .isInstanceOf(BankException.class)
                .hasMessage("Account not found.");
    }

    @Test
    void listTransactionsReturnsNewestFirstFromRepository() {
        List<BankTransaction> list = List.of(new BankTransaction(), new BankTransaction());
        when(transactions.findByCustomerCidOrderByDateDesc(CID)).thenReturn(list);

        assertThat(bankService.listTransactions(CID)).isSameAs(list);
    }

    // ---------- deposit / withdraw ----------

    @Test
    void depositAddsToBalanceAndRecordsTransactionForTheAccountOwner() {
        Account account = account("100.00");
        when(accounts.findByCustomerCidForUpdate(CID)).thenReturn(Optional.of(account));

        bankService.deposit(CID, new BigDecimal("25.50"));

        assertThat(account.getBalance()).isEqualByComparingTo("125.50");
        verify(transactions).save(transactionCaptor.capture());
        BankTransaction saved = transactionCaptor.getValue();
        assertThat(saved.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(saved.getAmount()).isEqualByComparingTo("25.50");
        assertThat(saved.getAccount()).isSameAs(account);
        // Regression: the customer (cid column) must be set, or the insert fails with "Column 'cid' cannot be null".
        assertThat(saved.getCustomer()).isSameAs(account.getCustomer());
        assertThat(saved.getDate()).isNotNull();
    }

    @Test
    void withdrawSubtractsFromBalanceAndRecordsTransactionForTheAccountOwner() {
        Account account = account("100.00");
        when(accounts.findByCustomerCidForUpdate(CID)).thenReturn(Optional.of(account));

        bankService.withdraw(CID, new BigDecimal("40.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("60.00");
        verify(transactions).save(transactionCaptor.capture());
        BankTransaction saved = transactionCaptor.getValue();
        assertThat(saved.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(saved.getAmount()).isEqualByComparingTo("40.00");
        assertThat(saved.getAccount()).isSameAs(account);
        assertThat(saved.getCustomer()).isSameAs(account.getCustomer());
        assertThat(saved.getDate()).isNotNull();
    }

    @Test
    void withdrawOfEntireBalanceIsAllowed() {
        Account account = account("40.00");
        when(accounts.findByCustomerCidForUpdate(CID)).thenReturn(Optional.of(account));

        bankService.withdraw(CID, new BigDecimal("40.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    void withdrawRejectsInsufficientFundsWithoutChangingBalance() {
        Account account = account("10.00");
        when(accounts.findByCustomerCidForUpdate(CID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> bankService.withdraw(CID, new BigDecimal("10.01")))
                .isInstanceOf(BankException.class)
                .hasMessage("Insufficient funds.");
        assertThat(account.getBalance()).isEqualByComparingTo("10.00");
        verifyNoInteractions(transactions);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "0.00", "-5"})
    void depositRejectsNonPositiveAmounts(BigDecimal amount) {
        assertThatThrownBy(() -> bankService.deposit(CID, amount))
                .isInstanceOf(BankException.class)
                .hasMessage("Amount must be greater than zero.");
        verifyNoInteractions(accounts, transactions);
    }

    @Test
    void withdrawRejectsMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> bankService.withdraw(CID, new BigDecimal("1.005")))
                .isInstanceOf(BankException.class)
                .hasMessage("Amount can have at most two decimal places.");
        verifyNoInteractions(accounts, transactions);
    }

    @Test
    void depositThrowsWhenAccountIsMissing() {
        when(accounts.findByCustomerCidForUpdate(CID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.deposit(CID, BigDecimal.ONE))
                .isInstanceOf(BankException.class)
                .hasMessage("Account not found.");
        verifyNoInteractions(transactions);
    }

    // ---------- profile ----------

    @Test
    void updateProfileChangesEmailUsernameAndPassword() {
        Customer ann = customer("ann");
        when(customers.findById(CID)).thenReturn(Optional.of(ann));
        when(customers.existsByUsername("annie")).thenReturn(false);

        bankService.updateProfile(CID, "new@example.com", "annie", "newpass");

        assertThat(ann.getEmail()).isEqualTo("new@example.com");
        assertThat(ann.getUsername()).isEqualTo("annie");
        assertThat(ann.getPassword()).isEqualTo("newpass");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void updateProfileKeepsPasswordWhenBlank(String password) {
        Customer ann = customer("ann");
        when(customers.findById(CID)).thenReturn(Optional.of(ann));

        bankService.updateProfile(CID, "new@example.com", "ann", password);

        assertThat(ann.getPassword()).isEqualTo("secret");
        assertThat(ann.getEmail()).isEqualTo("new@example.com");
        // Keeping the same username must not trip the "already taken" check.
        verify(customers, never()).existsByUsername(any());
    }

    @Test
    void updateProfileRejectsUsernameTakenBySomeoneElse() {
        Customer ann = customer("ann");
        when(customers.findById(CID)).thenReturn(Optional.of(ann));
        when(customers.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> bankService.updateProfile(CID, "new@example.com", "bob", null))
                .isInstanceOf(BankException.class)
                .hasMessage("Username is already taken.");
        assertThat(ann.getUsername()).isEqualTo("ann");
        assertThat(ann.getEmail()).isEqualTo("ann@example.com");
    }

    @Test
    void resetPasswordSetsNewPassword() {
        Customer ann = customer("ann");
        when(customers.findById(CID)).thenReturn(Optional.of(ann));

        bankService.resetPassword(CID, "fresh");

        assertThat(ann.getPassword()).isEqualTo("fresh");
    }

    // ---------- helpers ----------

    private static Customer customer(String username) {
        return Customer.builder()
                .cid(CID)
                .name("Ann Lee")
                .email(username + "@example.com")
                .username(username)
                .password("secret")
                .birthday("3/14/1995")
                .gender("f")
                .build();
    }

    private static Account account(String balance) {
        return Account.builder()
                .acctNum(7)
                .acctType(AccountType.CHECKING)
                .customer(customer("ann"))
                .balance(new BigDecimal(balance))
                .build();
    }
}
