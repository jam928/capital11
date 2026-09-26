package com.capital11.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public record AmountRequest(@NotNull @Schema(example = "25.00") BigDecimal amount) {
}
