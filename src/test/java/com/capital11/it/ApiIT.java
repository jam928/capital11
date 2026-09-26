package com.capital11.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** The JSON API end to end: HTTP -> interceptor -> controller -> service -> MySQL. */
class ApiIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void registerLoginTransactAndLogout() throws Exception {
        mvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Ann Lee", "email": "ann@example.com", "username": "ann", "password": "secret",
                         "birthday": "1995-03-14", "gender": "f", "accountType": "CHECKING"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.birthday").value("3/14/1995"));

        mvc.perform(get("/api/customers/username-availability").param("username", "ann"))
                .andExpect(jsonPath("$.available").value(false));

        MockHttpSession session = login("ann", "secret");

        mvc.perform(post("/api/me/deposits").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.25));

        mvc.perform(post("/api/me/withdrawals").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(80.25));

        mvc.perform(post("/api/me/withdrawals").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Insufficient funds."));

        mvc.perform(get("/api/me/account").session(session))
                .andExpect(jsonPath("$.type").value("CHECKING"))
                .andExpect(jsonPath("$.balance").value(80.25));

        mvc.perform(get("/api/me/transactions").session(session))
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(put("/api/me").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"annie@example.com\", \"username\": \"annie\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("annie"));

        mvc.perform(delete("/api/session").session(session)).andExpect(status().isNoContent());
        mvc.perform(get("/api/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpointsRequireLogin() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me/account")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/me/deposits").contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"nobody\", \"password\": \"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void openApiDocsDescribeTheApi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Capital 11 Bank API"))
                .andExpect(jsonPath("$.paths['/api/me/deposits']").exists());
    }

    private MockHttpSession login(String username, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }
}
