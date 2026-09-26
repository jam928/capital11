package com.capital11.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capital11.domain.Account;
import com.capital11.domain.AccountType;
import com.capital11.domain.BankTransaction;
import com.capital11.domain.Customer;
import com.capital11.domain.TransactionType;
import com.capital11.repository.AccountRepository;
import com.capital11.repository.BankTransactionRepository;
import com.capital11.repository.CustomerRepository;

@Service
@Transactional
public class BankService {

    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final BankTransactionRepository transactions;

    public BankService(CustomerRepository customers, AccountRepository accounts,
                       BankTransactionRepository transactions) {
        this.customers = customers;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public Customer register(String name, String email, String username, String password,
                             String birthday, String gender, AccountType accountType) {
        if (customers.existsByUsername(username)) {
            throw new BankException("Username is already taken.");
        }
        Customer customer = customers.save(
                new Customer(newCustomerId(), name, email, username, password, birthday, gender));
        accounts.save(new Account(accountType, customer));
        return customer;
    }

    @Transactional(readOnly = true)
    public Optional<Customer> authenticate(String username, String password) {
        return customers.findByUsername(username)
                .filter(c -> c.getPassword().equals(password));
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !customers.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(int cid) {
        return customers.findById(cid).orElseThrow(() -> new BankException("Customer not found."));
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findCustomer(String username) {
        return customers.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Account getAccount(int cid) {
        return accounts.findByCustomerCid(cid).orElseThrow(() -> new BankException("Account not found."));
    }

    @Transactional(readOnly = true)
    public List<BankTransaction> listTransactions(int cid) {
        return transactions.findByCustomerCidOrderByDateDesc(cid);
    }

    public void deposit(int cid, BigDecimal amount) {
        Account account = lockAccount(cid, amount);
        account.setBalance(account.getBalance().add(amount));
        transactions.save(new BankTransaction(TransactionType.DEPOSIT, account, amount, LocalDateTime.now()));
    }

    public void withdraw(int cid, BigDecimal amount) {
        Account account = lockAccount(cid, amount);
        BigDecimal newBalance = account.getBalance().subtract(amount);
        if (newBalance.signum() < 0) {
            throw new BankException("Insufficient funds.");
        }
        account.setBalance(newBalance);
        transactions.save(new BankTransaction(TransactionType.WITHDRAWAL, account, amount, LocalDateTime.now()));
    }

    /** Updates contact details; a blank password leaves the current password unchanged. */
    public void updateProfile(int cid, String email, String username, String password) {
        Customer customer = getCustomer(cid);
        if (!customer.getUsername().equals(username) && customers.existsByUsername(username)) {
            throw new BankException("Username is already taken.");
        }
        customer.setEmail(email);
        customer.setUsername(username);
        if (password != null && !password.isBlank()) {
            customer.setPassword(password);
        }
    }

    public void resetPassword(int cid, String password) {
        getCustomer(cid).setPassword(password);
    }

    private Account lockAccount(int cid, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BankException("Amount must be greater than zero.");
        }
        if (amount.scale() > 2) {
            throw new BankException("Amount can have at most two decimal places.");
        }
        return accounts.findByCustomerCidForUpdate(cid)
                .orElseThrow(() -> new BankException("Account not found."));
    }

    private int newCustomerId() {
        int cid;
        do {
            cid = ThreadLocalRandom.current().nextInt(1000, 100000);
        } while (customers.existsById(cid));
        return cid;
    }
}
