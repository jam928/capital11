package com.capital11.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.capital11.domain.AccountType;
import com.capital11.domain.Customer;
import com.capital11.mapper.DtoMapper;
import com.capital11.service.BankException;
import com.capital11.service.BankService;
import com.capital11.web.SessionKeys;

@ExtendWith(MockitoExtension.class)
class AuthApiControllerTest {

    private static final String REGISTER_JSON = """
            {"name": "Ann Lee", "email": "ann@example.com", "username": "ann", "password": "secret",
             "birthday": "1995-03-14", "gender": "f", "accountType": "SAVINGS"}
            """;

    @Mock
    private BankService bankService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new AuthApiController(bankService, Mappers.getMapper(DtoMapper.class)))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void loginReturnsCustomerAndStartsSession() throws Exception {
        when(bankService.authenticate("ann", "secret")).thenReturn(Optional.of(customer()));

        mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"ann\", \"password\": \"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cid").value(4242))
                .andExpect(jsonPath("$.username").value("ann"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(request().sessionAttribute(SessionKeys.CUSTOMER_ID, 4242));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        when(bankService.authenticate("ann", "wrong")).thenReturn(Optional.empty());

        mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"ann\", \"password\": \"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(request().sessionAttributeDoesNotExist(SessionKeys.CUSTOMER_ID));
    }

    @Test
    void loginWithBlankFieldsIsBadRequestListingErrors() throws Exception {
        mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"\", \"password\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid request."))
                .andExpect(jsonPath("$.errors.length()").value(2));

        verify(bankService, never()).authenticate(any(), any());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mvc.perform(delete("/api/session").session(session)).andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void registerMapsRequestToCustomerAndReturnsCreated() throws Exception {
        when(bankService.register(any(), eq(AccountType.SAVINGS))).thenAnswer(call -> {
            Customer customer = call.getArgument(0);
            customer.setCid(4242);
            return customer;
        });

        mvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(REGISTER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cid").value(4242))
                .andExpect(jsonPath("$.birthday").value("3/14/1995"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(bankService).register(customerCaptor.capture(), eq(AccountType.SAVINGS));
        assertThat(customerCaptor.getValue().getPassword()).isEqualTo("secret");
        assertThat(customerCaptor.getValue().getBirthday()).isEqualTo("3/14/1995");
    }

    @Test
    void registerWithTakenUsernameIsBadRequest() throws Exception {
        when(bankService.register(any(), any())).thenThrow(new BankException("Username is already taken."));

        mvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(REGISTER_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Username is already taken."));
    }

    @Test
    void registerRejectsFutureBirthdayAndMissingFields() throws Exception {
        mvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"bad\", \"birthday\": \"2999-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        verify(bankService, never()).register(any(), any());
    }

    @Test
    void usernameAvailability() throws Exception {
        when(bankService.isUsernameAvailable("ann")).thenReturn(false);

        mvc.perform(get("/api/customers/username-availability").param("username", "ann"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ann"))
                .andExpect(jsonPath("$.available").value(false));
    }

    private static Customer customer() {
        return new Customer(4242, "Ann Lee", "ann@example.com", "ann", "secret", "3/14/1995", "f");
    }
}
