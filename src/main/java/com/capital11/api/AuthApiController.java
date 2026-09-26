package com.capital11.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.capital11.mapper.DtoMapper;
import com.capital11.dto.request.LoginRequest;
import com.capital11.dto.request.RegisterRequest;
import com.capital11.dto.response.CustomerResponse;
import com.capital11.dto.response.UsernameAvailabilityResponse;
import com.capital11.service.BankService;
import com.capital11.web.SessionKeys;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth", description = "Registration and session login/logout")
public class AuthApiController {

    private final BankService bankService;
    private final DtoMapper mapper;

    public AuthApiController(BankService bankService, DtoMapper mapper) {
        this.bankService = bankService;
        this.mapper = mapper;
    }

    @PostMapping("/session")
    @Operation(summary = "Log in", description = "Starts a session; the JSESSIONID cookie authenticates later calls.")
    @ApiResponse(responseCode = "200", description = "Logged in")
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
    public ResponseEntity<CustomerResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return bankService.authenticate(request.username(), request.password())
                .map(customer -> {
                    session.setAttribute(SessionKeys.CUSTOMER_ID, customer.getCid());
                    return ResponseEntity.ok(mapper.toResponse(customer));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @DeleteMapping("/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Log out")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register", description = "Creates a customer and their account. Does not log in.")
    @ApiResponse(responseCode = "400", description = "Invalid request or username taken")
    public CustomerResponse register(@Valid @RequestBody RegisterRequest request) {
        return mapper.toResponse(bankService.register(mapper.toCustomer(request), request.accountType()));
    }

    @GetMapping("/customers/username-availability")
    @Operation(summary = "Check whether a username is free")
    public UsernameAvailabilityResponse usernameAvailability(@RequestParam String username) {
        return new UsernameAvailabilityResponse(username, bankService.isUsernameAvailable(username));
    }
}
