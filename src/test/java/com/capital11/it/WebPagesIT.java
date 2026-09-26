package com.capital11.it;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** The Thymeleaf pages end to end, including real template rendering. */
class WebPagesIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void publicPagesRender() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back")))
                .andExpect(content().string(containsString("data-theme-toggle")));
        mvc.perform(get("/register")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Open an account")))
                .andExpect(content().string(containsString("College")));
        mvc.perform(get("/forgot-password")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Reset your password")));
    }

    @Test
    void signedInPagesRedirectToLoginWithoutSession() throws Exception {
        for (String page : new String[] {"/main", "/deposit", "/withdraw", "/profile", "/profile/edit"}) {
            mvc.perform(get(page)).andExpect(redirectedUrl("/login"));
        }
    }

    @Test
    void registerLoginDepositWithdrawAndViewDashboard() throws Exception {
        mvc.perform(registration("ann"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", "Account created. Please log in."));

        MockHttpSession session = login("ann", "secret");

        mvc.perform(post("/deposit").session(session).param("amount", "1250.50"))
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("message", "Deposited $1250.50."));
        mvc.perform(post("/withdraw").session(session).param("amount", "80"))
                .andExpect(redirectedUrl("/main"));

        mvc.perform(get("/main").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hi, <span>Ann Lee</span>")))
                .andExpect(content().string(containsString("$1,170.50")))
                .andExpect(content().string(containsString("Savings account")))
                .andExpect(content().string(containsString("+$1,250.50")))
                .andExpect(content().string(containsString("−$80.00")));
    }

    @Test
    void withdrawalErrorIsShownOnThePage() throws Exception {
        mvc.perform(registration("ann"));
        MockHttpSession session = login("ann", "secret");

        mvc.perform(post("/withdraw").session(session).param("amount", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Insufficient funds.")));
    }

    @Test
    void registrationErrorsAreShownNextToFields() throws Exception {
        mvc.perform(registration("ann"));

        mvc.perform(registration("ann"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Username is already taken.")));
        mvc.perform(post("/register").param("name", "").param("email", "bad").param("username", "x")
                        .param("password", "a").param("confirmPassword", "b").param("accountType", "CHECKING"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("passwords do not match")))
                .andExpect(content().string(containsString("Enter a valid birthday")));
    }

    @Test
    void usernameCheckReflectsDatabase() throws Exception {
        mvc.perform(get("/register/username-check").param("username", "ann"))
                .andExpect(content().string(containsString("Available")));
        mvc.perform(registration("ann"));
        mvc.perform(get("/register/username-check").param("username", "ann"))
                .andExpect(content().string(containsString("Taken")));
    }

    @Test
    void profilePagesShowAndUpdateDetails() throws Exception {
        mvc.perform(registration("ann"));
        MockHttpSession session = login("ann", "secret");

        mvc.perform(get("/profile").session(session))
                .andExpect(content().string(containsString("ann@example.com")))
                .andExpect(content().string(containsString("Female")))
                .andExpect(content().string(containsString("3/14/1995")));

        mvc.perform(post("/profile/edit").session(session)
                        .param("email", "annie@example.com").param("username", "annie").param("password", ""))
                .andExpect(redirectedUrl("/main"));

        mvc.perform(get("/profile/edit").session(session))
                .andExpect(content().string(containsString("value=\"annie@example.com\"")))
                .andExpect(content().string(not(containsString("ann@example.com\""))));
    }

    @Test
    void forgotPasswordFlowResetsPassword() throws Exception {
        mvc.perform(registration("ann"));

        MockHttpSession session = (MockHttpSession) mvc.perform(post("/forgot-password").param("username", "ann"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Choose a new password")))
                .andReturn().getRequest().getSession();

        mvc.perform(post("/reset-password").session(session).param("password", "fresh"))
                .andExpect(redirectedUrl("/login"));

        mvc.perform(post("/login").param("username", "ann").param("password", "secret"))
                .andExpect(content().string(containsString("Invalid username or password.")));
        login("ann", "fresh");
    }

    @Test
    void logoutEndsSession() throws Exception {
        mvc.perform(registration("ann"));
        MockHttpSession session = login("ann", "secret");

        mvc.perform(get("/logout").session(session)).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/main").session(session)).andExpect(redirectedUrl("/login"));
    }

    private static MockHttpServletRequestBuilder registration(String username) {
        return post("/register")
                .param("name", "Ann Lee")
                .param("email", username + "@example.com")
                .param("username", username)
                .param("password", "secret")
                .param("confirmPassword", "secret")
                .param("birthMonth", "3")
                .param("birthDay", "14")
                .param("birthYear", "1995")
                .param("gender", "f")
                .param("accountType", "SAVINGS");
    }

    private MockHttpSession login(String username, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post("/login").param("username", username).param("password", password))
                .andExpect(redirectedUrl("/main"))
                .andReturn().getRequest().getSession();
    }
}
