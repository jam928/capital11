package com.capital11.api;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.capital11.mapper.DtoMapper;
import com.capital11.dto.request.AmountRequest;
import com.capital11.dto.request.ProfileUpdateRequest;
import com.capital11.dto.response.AccountResponse;
import com.capital11.dto.response.CustomerResponse;
import com.capital11.dto.response.TransactionResponse;
import com.capital11.service.BankService;
import com.capital11.web.SessionKeys;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** The logged-in customer's profile, account and transactions. */
@RestController
@RequestMapping("/api/me")
@Tag(name = "Me", description = "The logged-in customer (requires POST /api/session first)")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
@ApiResponse(responseCode = "401", description = "Not logged in")
public class MeApiController {

    private final BankService bankService;
    private final DtoMapper mapper;

    public MeApiController(BankService bankService, DtoMapper mapper) {
        this.bankService = bankService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Get profile")
    public CustomerResponse profile(@Parameter(hidden = true) @SessionAttribute(SessionKeys.CUSTOMER_ID) int cid) {
        return mapper.toResponse(bankService.getCustomer(cid));
    }

    @PutMapping
    @Operation(summary = "Update profile")
    @ApiResponse(responseCode = "400", description = "Invalid request or username taken")
    public CustomerResponse updateProfile(@Parameter(hidden = true) @SessionAttribute(SessionKeys.CUSTOMER_ID) int cid,
                                          @Valid @RequestBody ProfileUpdateRequest request) {
        bankService.updateProfile(cid, request.email().trim(), request.username().trim(), request.password());
        return mapper.toResponse(bankService.getCustomer(cid));
    }

    @GetMapping("/account")
    @Operation(summary = "Get account and balance")
    public AccountResponse account(@Parameter(hidden = true) @SessionAttribute(SessionKeys.CUSTOMER_ID) int cid) {
        return mapper.toResponse(bankService.getAccount(cid));
    }

    @GetMapping("/transactions")
    @Operation(summary = "List transactions, newest first")
    public List<TransactionResponse> transactions(
            @Parameter(hidden = true) @SessionAttribute(SessionKeys.CUSTOMER_ID) int cid) {
        return mapper.toTransactionResponses(bankService.listTransactions(cid));
    }

    @PostMapping("/deposits")
    @Operation(summary = "Deposit", description = "Returns the account with its new balance.")
    @ApiResponse(responseCode = "400", description = "Amount not positive or more than two decimal places")
    public AccountResponse deposit(@Parameter(hidden = true) @SessionAttribute(SessionKeys.CUSTOMER_ID) int cid,
                                   @Valid @RequestBody AmountRequest request) {
        bankService.deposit(cid, request.amount());
        return mapper.toResponse(bankService.getAccount(cid));
    }

    @PostMapping("/withdrawals")
    @Operation(summary = "Withdraw", description = "Returns the account with its new balance.")
    @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds")
    public AccountResponse withdraw(@Parameter(hidden = true) @SessionAttribute(SessionKeys.CUSTOMER_ID) int cid,
                                    @Valid @RequestBody AmountRequest request) {
        bankService.withdraw(cid, request.amount());
        return mapper.toResponse(bankService.getAccount(cid));
    }
}
