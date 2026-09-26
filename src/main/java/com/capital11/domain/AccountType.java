package com.capital11.domain;

public enum AccountType {
    CHECKING("Checking"),
    SAVINGS("Savings"),
    COLLEGE("College");

    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
