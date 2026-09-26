package com.capital11.web;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.capital11.domain.Account;
import com.capital11.domain.AccountType;
import com.capital11.domain.BankTransaction;
import com.capital11.domain.Customer;
import com.capital11.domain.TransactionType;
import com.capital11.dto.response.AccountResponse;
import com.capital11.dto.response.CustomerResponse;
import com.capital11.dto.response.TransactionResponse;
import com.capital11.mapper.DtoMapper;
import com.capital11.service.BankException;
import com.capital11.service.BankService;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private static final int CID = 4242;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 26, 14, 30);

    @Mock
    private BankService bankService;

    private MockMvc mvc;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        AccountController controller = new AccountController(bankService, Mappers.getMapper(DtoMapper.class));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
                .build();
        session = new MockHttpSession();
        session.setAttribute(SessionKeys.CUSTOMER_ID, CID);
    }

    @Test
    void mainShowsMappedCustomerAccountAndTransactions() throws Exception {
        when(bankService.getCustomer(CID)).thenReturn(customer());
        when(bankService.getAccount(CID)).thenReturn(account());
        when(bankService.listTransactions(CID)).thenReturn(List.of(BankTransaction.builder()
                .tid(1).type(TransactionType.DEPOSIT).amount(new BigDecimal("50.00")).date(NOW).build()));

        mvc.perform(get("/main").session(session))
                .andExpect(view().name("main"))
                .andExpect(model().attribute("customer", customerResponse()))
                .andExpect(model().attribute("account",
                        new AccountResponse(7, AccountType.CHECKING, new BigDecimal("100.00"))))
                .andExpect(model().attribute("transactions",
                        List.of(new TransactionResponse(1, TransactionType.DEPOSIT, new BigDecimal("50.00"), NOW))));
    }

    @Test
    void depositAndWithdrawPages() throws Exception {
        mvc.perform(get("/deposit").session(session)).andExpect(view().name("deposit"));
        mvc.perform(get("/withdraw").session(session)).andExpect(view().name("withdraw"));
    }

    @Test
    void depositRedirectsToMainWithMessage() throws Exception {
        mvc.perform(post("/deposit").param("amount", " 25.5 ").session(session))
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("message", "Deposited $25.50."));

        verify(bankService).deposit(CID, new BigDecimal("25.5"));
    }

    @Test
    void depositRejectsAmountThatIsNotANumber() throws Exception {
        mvc.perform(post("/deposit").param("amount", "abc").session(session))
                .andExpect(view().name("deposit"))
                .andExpect(model().attribute("error", "Enter a valid amount, e.g. 25.00"));

        verify(bankService, never()).deposit(anyInt(), any());
    }

    @Test
    void withdrawRedirectsToMainWithMessage() throws Exception {
        mvc.perform(post("/withdraw").param("amount", "40").session(session))
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("message", "Withdrew $40.00."));

        verify(bankService).withdraw(CID, new BigDecimal("40"));
    }

    @Test
    void withdrawShowsBusinessRuleErrors() throws Exception {
        doThrow(new BankException("Insufficient funds.")).when(bankService).withdraw(CID, new BigDecimal("1000"));

        mvc.perform(post("/withdraw").param("amount", "1000").session(session))
                .andExpect(view().name("withdraw"))
                .andExpect(model().attribute("error", "Insufficient funds."));
    }

    @Test
    void profilePages() throws Exception {
        when(bankService.getCustomer(CID)).thenReturn(customer());

        mvc.perform(get("/profile").session(session))
                .andExpect(view().name("viewinfo"))
                .andExpect(model().attribute("customer", customerResponse()));
        mvc.perform(get("/profile/edit").session(session))
                .andExpect(view().name("update"))
                .andExpect(model().attribute("customer", customerResponse()));
    }

    @Test
    void editProfileTrimsInputAndRedirectsToMain() throws Exception {
        mvc.perform(post("/profile/edit")
                        .param("email", " new@example.com ")
                        .param("username", " annie ")
                        .param("password", "newpass")
                        .session(session))
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("message", "Profile updated."));

        verify(bankService).updateProfile(CID, "new@example.com", "annie", "newpass");
    }

    @Test
    void editProfileShowsErrorAndCurrentDetails() throws Exception {
        doThrow(new BankException("Username is already taken."))
                .when(bankService).updateProfile(CID, "ann@example.com", "bob", null);
        when(bankService.getCustomer(CID)).thenReturn(customer());

        mvc.perform(post("/profile/edit")
                        .param("email", "ann@example.com")
                        .param("username", "bob")
                        .session(session))
                .andExpect(view().name("update"))
                .andExpect(model().attribute("error", "Username is already taken."))
                .andExpect(model().attribute("customer", customerResponse()));
    }

    @Test
    void mainFailsWhenCustomerHasNoAccount() {
        when(bankService.getCustomer(CID)).thenReturn(customer());
        when(bankService.getAccount(CID)).thenThrow(new BankException("Account not found."));

        assertThatThrownBy(() -> mvc.perform(get("/main").session(session)))
                .hasRootCauseInstanceOf(BankException.class);
        verify(bankService, never()).listTransactions(anyInt());
    }

    private static Customer customer() {
        return new Customer(CID, "Ann Lee", "ann@example.com", "ann", "secret", "3/14/1995", "f");
    }

    private static CustomerResponse customerResponse() {
        return new CustomerResponse(CID, "Ann Lee", "ann@example.com", "ann", "3/14/1995", "f");
    }

    private static Account account() {
        return Account.builder().acctNum(7).acctType(AccountType.CHECKING).balance(new BigDecimal("100.00")).build();
    }
}
