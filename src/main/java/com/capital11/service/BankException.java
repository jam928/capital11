package com.capital11.service;

/** A business-rule failure whose message is safe to show to the user. */
public class BankException extends RuntimeException {

    public BankException(String message) {
        super(message);
    }
}
