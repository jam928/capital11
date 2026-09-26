package com.capital11.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.capital11.domain.Account;
import com.capital11.domain.AccountType;
import com.capital11.domain.BankTransaction;
import com.capital11.domain.Customer;
import com.capital11.domain.TransactionType;
import com.capital11.mapper.DtoMapper;
import com.capital11.service.BankException;
import com.capital11.service.BankService;
import com.capital11.web.SessionKeys;

@ExtendWith(MockitoExtension.class)
class MeApiControllerTest {

    private static final int CID = 4242;

    @Mock
    private BankService bankService;

    private MockMvc mvc;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new MeApiController(bankService, Mappers.getMapper(DtoMapper.class)))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        session = new MockHttpSession();
        session.setAttribute(SessionKeys.CUSTOMER_ID, CID);
    }

    @Test
    void profile() throws Exception {
        when(bankService.getCustomer(CID)).thenReturn(customer("ann"));

        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cid").value(CID))
                .andExpect(jsonPath("$.name").value("Ann Lee"))
                .andExpect(jsonPath("$.email").value("ann@example.com"))
                .andExpect(jsonPath("$.gender").value("f"));
    }

    @Test
    void updateProfileTrimsUsernameAndReturnsUpdatedProfile() throws Exception {
        when(bankService.getCustomer(CID)).thenReturn(customer("annie"));

        mvc.perform(put("/api/me").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"annie@example.com\", \"username\": \" annie \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("annie"));

        verify(bankService).updateProfile(CID, "annie@example.com", "annie", null);
    }

    @Test
    void updateProfileWithTakenUsernameIsBadRequest() throws Exception {
        doThrow(new BankException("Username is already taken."))
                .when(bankService).updateProfile(CID, "ann@example.com", "bob", "pw");

        mvc.perform(put("/api/me").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ann@example.com\", \"username\": \"bob\", \"password\": \"pw\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Username is already taken."));
    }

    @Test
    void account() throws Exception {
        when(bankService.getAccount(CID)).thenReturn(account("100.00"));

        mvc.perform(get("/api/me/account").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctNum").value(7))
                .andExpect(jsonPath("$.type").value("CHECKING"))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void transactions() throws Exception {
        when(bankService.listTransactions(CID)).thenReturn(List.of(
                BankTransaction.builder().tid(2).type(TransactionType.WITHDRAWAL)
                        .amount(new BigDecimal("5.00")).date(LocalDateTime.of(2026, 9, 26, 15, 0)).build(),
                BankTransaction.builder().tid(1).type(TransactionType.DEPOSIT)
                        .amount(new BigDecimal("50.00")).date(LocalDateTime.of(2026, 9, 26, 14, 0)).build()));

        mvc.perform(get("/api/me/transactions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tid").value(2))
                .andExpect(jsonPath("$[0].type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$[1].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].amount").value(50.00));
    }

    @Test
    void depositReturnsAccountWithNewBalance() throws Exception {
        when(bankService.getAccount(CID)).thenReturn(account("125.00"));

        mvc.perform(post("/api/me/deposits").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 25.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(125.00));

        verify(bankService).deposit(CID, new BigDecimal("25.00"));
    }

    @Test
    void depositWithoutAmountIsBadRequest() throws Exception {
        mvc.perform(post("/api/me/deposits").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.startsWith("amount:")));

        verify(bankService, never()).deposit(anyInt(), any());
    }

    @Test
    void withdrawReturnsAccountWithNewBalance() throws Exception {
        when(bankService.getAccount(CID)).thenReturn(account("60.00"));

        mvc.perform(post("/api/me/withdrawals").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(60.00));

        verify(bankService).withdraw(CID, new BigDecimal("40"));
    }

    @Test
    void withdrawWithInsufficientFundsIsBadRequest() throws Exception {
        doThrow(new BankException("Insufficient funds.")).when(bankService).withdraw(CID, new BigDecimal("999"));

        mvc.perform(post("/api/me/withdrawals").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Insufficient funds."));
    }

    private static Customer customer(String username) {
        return new Customer(CID, "Ann Lee", username + "@example.com", username, "secret", "3/14/1995", "f");
    }

    private static Account account(String balance) {
        return Account.builder().acctNum(7).acctType(AccountType.CHECKING).balance(new BigDecimal(balance)).build();
    }
}
