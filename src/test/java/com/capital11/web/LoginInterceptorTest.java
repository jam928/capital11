package com.capital11.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

class LoginInterceptorTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("GET", "/capital11/main");
        request.setContextPath("/capital11");
        response = new MockHttpServletResponse();
    }

    @Test
    void letsLoggedInCustomerThrough() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.CUSTOMER_ID, 4242);
        request.setSession(session);

        assertThat(new LoginInterceptor().preHandle(request, response, new Object())).isTrue();
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    void redirectsToLoginWithoutSession() throws Exception {
        assertThat(new LoginInterceptor().preHandle(request, response, new Object())).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/capital11/login");
    }

    @Test
    void redirectsToLoginWhenSessionHasNoCustomer() throws Exception {
        request.setSession(new MockHttpSession());

        assertThat(new LoginInterceptor().preHandle(request, response, new Object())).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/capital11/login");
    }

    @Test
    void webConfigGuardsTheSignedInPages() {
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class, RETURNS_SELF);
        when(registry.addInterceptor(any())).thenReturn(registration);

        new WebConfig().addInterceptors(registry);

        ArgumentCaptor<HandlerInterceptor> interceptor = ArgumentCaptor.forClass(HandlerInterceptor.class);
        verify(registry).addInterceptor(interceptor.capture());
        assertThat(interceptor.getValue()).isInstanceOf(LoginInterceptor.class);
        verify(registration).addPathPatterns("/main", "/deposit", "/withdraw", "/profile", "/profile/**");
    }
}
