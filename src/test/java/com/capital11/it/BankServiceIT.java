package com.capital11.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.capital11.domain.AccountType;
import com.capital11.domain.BankTransaction;
import com.capital11.domain.Customer;
import com.capital11.domain.TransactionType;
import com.capital11.service.BankException;
import com.capital11.service.BankService;

class BankServiceIT extends AbstractIntegrationTest {

    @Autowired
    private BankService bankService;

    @Test
    void registerSavesCustomerAndOpensAccountWithZeroBalance() {
        Customer ann = register("ann");

        Map<String, Object> row = jdbc.queryForMap(
                "select c.username, c.birthday, a.acct_type, a.balance from customer c join account a on a.cid = c.cid "
                        + "where c.cid = ?", ann.getCid());
        assertThat(row.get("username")).isEqualTo("ann");
        assertThat(row.get("birthday")).isEqualTo("3/14/1995");
        assertThat(row.get("acct_type")).isEqualTo("SAVINGS");
        assertThat((BigDecimal) row.get("balance")).isEqualByComparingTo("0.00");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        register("ann");

        assertThatThrownBy(() -> register("ann"))
                .isInstanceOf(BankException.class)
                .hasMessage("Username is already taken.");
        assertThat(jdbc.queryForObject("select count(*) from customer", Integer.class)).isEqualTo(1);
    }

    @Test
    void depositAndWithdrawUpdateBalanceAndRecordTransactionsWithCustomerId() {
        int cid = register("ann").getCid();

        bankService.deposit(cid, new BigDecimal("100.25"));
        bankService.withdraw(cid, new BigDecimal("20.00"));

        assertThat(bankService.getAccount(cid).getBalance()).isEqualByComparingTo("80.25");
        // Regression for "Column 'cid' cannot be null": every transaction row carries the customer's cid.
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select tr_type, amount, cid from transaction order by tid");
        assertThat(rows).hasSize(2);
        assertThat(rows).allSatisfy(row -> assertThat(row.get("cid")).isEqualTo(cid));
        assertThat(rows.get(0).get("tr_type")).isEqualTo("DEPOSIT");
        assertThat(rows.get(1).get("tr_type")).isEqualTo("WITHDRAWAL");
        assertThat((BigDecimal) rows.get(1).get("amount")).isEqualByComparingTo("20.00");
    }

    @Test
    void failedWithdrawalLeavesBalanceAndHistoryUntouched() {
        int cid = register("ann").getCid();
        bankService.deposit(cid, new BigDecimal("10.00"));

        assertThatThrownBy(() -> bankService.withdraw(cid, new BigDecimal("10.01")))
                .isInstanceOf(BankException.class)
                .hasMessage("Insufficient funds.");

        assertThat(bankService.getAccount(cid).getBalance()).isEqualByComparingTo("10.00");
        assertThat(jdbc.queryForObject("select count(*) from transaction", Integer.class)).isEqualTo(1);
    }

    @Test
    void listTransactionsReturnsOnlyThisCustomersTransactionsNewestFirst() {
        int ann = register("ann").getCid();
        int bob = register("bob").getCid();
        bankService.deposit(ann, new BigDecimal("1.00"));
        bankService.deposit(bob, new BigDecimal("99.00"));
        bankService.withdraw(ann, new BigDecimal("0.50"));
        // Same-second inserts would tie on dateTrans, so spread them out explicitly.
        jdbc.update("update transaction set dateTrans = date_sub(dateTrans, interval 1 hour) where tr_type = 'DEPOSIT'");

        List<BankTransaction> history = bankService.listTransactions(ann);

        assertThat(history).extracting(BankTransaction::getType)
                .containsExactly(TransactionType.WITHDRAWAL, TransactionType.DEPOSIT);
    }

    @Test
    void updateProfileAndResetPasswordArePersisted() {
        int cid = register("ann").getCid();

        bankService.updateProfile(cid, "annie@example.com", "annie", "");
        assertThat(bankService.authenticate("annie", "secret")).isPresent();

        bankService.resetPassword(cid, "fresh");
        assertThat(bankService.authenticate("annie", "secret")).isEmpty();
        assertThat(bankService.authenticate("annie", "fresh")).isPresent();
        assertThat(bankService.getCustomer(cid).getEmail()).isEqualTo("annie@example.com");
    }

    @Test
    void updateProfileRejectsUsernameOfAnotherCustomer() {
        int cid = register("ann").getCid();
        register("bob");

        assertThatThrownBy(() -> bankService.updateProfile(cid, "ann@example.com", "bob", null))
                .isInstanceOf(BankException.class);
        assertThat(bankService.getCustomer(cid).getUsername()).isEqualTo("ann");
    }

    private Customer register(String username) {
        Customer customer = Customer.builder()
                .name("Test " + username)
                .email(username + "@example.com")
                .username(username)
                .password("secret")
                .birthday("3/14/1995")
                .gender("f")
                .build();
        return bankService.register(customer, AccountType.SAVINGS);
    }
}
