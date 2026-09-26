package com.capital11.dto.response;

import java.math.BigDecimal;

import com.capital11.domain.AccountType;

public record AccountResponse(int acctNum, AccountType type, BigDecimal balance) {
}
