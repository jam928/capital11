package com.capital11.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.capital11.domain.TransactionType;

public record TransactionResponse(int tid, TransactionType type, BigDecimal amount, LocalDateTime date) {
}
