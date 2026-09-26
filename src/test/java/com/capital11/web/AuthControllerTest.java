package com.capital11.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.capital11.domain.AccountType;
import com.capital11.domain.Customer;
import com.capital11.dto.request.RegistrationForm;
import com.capital11.mapper.DtoMapper;
import com.capital11.service.BankException;
import com.capital11.service.BankService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private BankService bankService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(bankService, Mappers.getMapper(DtoMapper.class));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                // A prefix keeps view names like "login" from looking like a circular forward to /login.
                .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
                .build();
    }

    @Test
    void rootRedirectsToLogin() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginPage() throws Exception {
        mvc.perform(get("/login")).andExpect(view().name("login"));
    }

    @Test
    void loginStoresCustomerIdInSessionAndRedirectsToMain() throws Exception {
        when(bankService.authenticate("ann", "secret")).thenReturn(Optional.of(customer()));

        mvc.perform(post("/login").param("username", "ann").param("password", "secret"))
                .andExpect(redirectedUrl("/main"))
                .andExpect(request().sessionAttribute(SessionKeys.CUSTOMER_ID, 4242));
    }

    @Test
    void failedLoginShowsError() throws Exception {
        when(bankService.authenticate("ann", "wrong")).thenReturn(Optional.empty());

        mvc.perform(post("/login").param("username", "ann").param("password", "wrong"))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Invalid username or password."))
                .andExpect(request().sessionAttributeDoesNotExist(SessionKeys.CUSTOMER_ID));
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.CUSTOMER_ID, 4242);

        mvc.perform(get("/logout").session(session)).andExpect(redirectedUrl("/login"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void registerPageHasEmptyFormAndAccountTypes() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("accountTypes", AccountType.values()));
    }

    @Test
    void registerMapsFormToCustomerAndRedirectsToLogin() throws Exception {
        mvc.perform(post("/register")
                        .param("name", "Ann Lee")
                        .param("email", "ann@example.com")
                        .param("username", "ann")
                        .param("password", "secret")
                        .param("confirmPassword", "secret")
                        .param("birthMonth", "3")
                        .param("birthDay", "14")
                        .param("birthYear", "1995")
                        .param("gender", "f")
                        .param("accountType", "SAVINGS"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", "Account created. Please log in."));

        verify(bankService).register(customerCaptor.capture(), eq(AccountType.SAVINGS));
        Customer customer = customerCaptor.getValue();
        assertThat(customer.getName()).isEqualTo("Ann Lee");
        assertThat(customer.getEmail()).isEqualTo("ann@example.com");
        assertThat(customer.getUsername()).isEqualTo("ann");
        assertThat(customer.getPassword()).isEqualTo("secret");
        assertThat(customer.getBirthday()).isEqualTo("3/14/1995");
        assertThat(customer.getGender()).isEqualTo("f");
    }

    @Test
    void registerWithInvalidFormRedisplaysFormWithoutSaving() throws Exception {
        mvc.perform(post("/register")
                        .param("name", "")
                        .param("email", "not-an-email")
                        .param("username", "ann")
                        .param("password", "secret")
                        .param("confirmPassword", "different")
                        .param("birthMonth", "13")
                        .param("birthDay", "14")
                        .param("birthYear", "1995")
                        .param("gender", "")
                        .param("accountType", "CHECKING"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form",
                        "name", "email", "passwordConfirmed", "birthMonth", "gender"));

        verify(bankService, never()).register(any(), any());
    }

    @Test
    void registerWithTakenUsernameShowsErrorOnUsernameField() throws Exception {
        when(bankService.register(any(), any())).thenThrow(new BankException("Username is already taken."));

        mvc.perform(post("/register")
                        .param("name", "Ann Lee")
                        .param("email", "ann@example.com")
                        .param("username", "ann")
                        .param("password", "secret")
                        .param("confirmPassword", "secret")
                        .param("birthMonth", "3")
                        .param("birthDay", "14")
                        .param("birthYear", "1995")
                        .param("gender", "f")
                        .param("accountType", "CHECKING"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrorCode("form", "username", "taken"));
    }

    @Test
    void usernameCheckReportsAvailable() throws Exception {
        when(bankService.isUsernameAvailable("free")).thenReturn(true);

        mvc.perform(get("/register/username-check").param("username", "free"))
                .andExpect(content().string("<span class=\"available\">Available</span>"));
    }

    @Test
    void usernameCheckReportsTaken() throws Exception {
        when(bankService.isUsernameAvailable("ann")).thenReturn(false);

        mvc.perform(get("/register/username-check").param("username", "ann"))
                .andExpect(content().string("<span class=\"taken\">Taken</span>"));
    }

    @Test
    void forgotPasswordPage() throws Exception {
        mvc.perform(get("/forgot-password")).andExpect(view().name("verifyReset"));
    }

    @Test
    void verifyUsernameRemembersCustomerForReset() throws Exception {
        when(bankService.findCustomer("ann")).thenReturn(Optional.of(customer()));

        mvc.perform(post("/forgot-password").param("username", "ann"))
                .andExpect(view().name("reset"))
                .andExpect(model().attribute("username", "ann"))
                .andExpect(request().sessionAttribute(SessionKeys.RESET_CUSTOMER_ID, 4242));
    }

    @Test
    void verifyUnknownUsernameShowsError() throws Exception {
        when(bankService.findCustomer("nobody")).thenReturn(Optional.empty());

        mvc.perform(post("/forgot-password").param("username", "nobody"))
                .andExpect(view().name("verifyReset"))
                .andExpect(model().attribute("error", "No user found with that username."))
                .andExpect(request().sessionAttributeDoesNotExist(SessionKeys.RESET_CUSTOMER_ID));
    }

    @Test
    void resetPasswordWithoutVerifiedUsernameRedirectsToForgotPassword() throws Exception {
        mvc.perform(post("/reset-password").param("password", "fresh"))
                .andExpect(redirectedUrl("/forgot-password"));

        verify(bankService, never()).resetPassword(anyInt(), anyString());
    }

    @Test
    void resetPasswordRejectsBlankPassword() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.RESET_CUSTOMER_ID, 4242);

        mvc.perform(post("/reset-password").param("password", "  ").session(session))
                .andExpect(view().name("reset"))
                .andExpect(model().attribute("error", "Password cannot be blank."));

        verify(bankService, never()).resetPassword(anyInt(), anyString());
        assertThat(session.getAttribute(SessionKeys.RESET_CUSTOMER_ID)).isEqualTo(4242);
    }

    @Test
    void resetPasswordUpdatesPasswordAndClearsResetState() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.RESET_CUSTOMER_ID, 4242);

        mvc.perform(post("/reset-password").param("password", "fresh").session(session))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", "Password updated. Please log in."));

        verify(bankService).resetPassword(4242, "fresh");
        assertThat(session.getAttribute(SessionKeys.RESET_CUSTOMER_ID)).isNull();
    }

    private static Customer customer() {
        return new Customer(4242, "Ann Lee", "ann@example.com", "ann", "secret", "3/14/1995", "f");
    }
}
