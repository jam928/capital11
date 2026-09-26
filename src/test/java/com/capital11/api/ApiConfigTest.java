package com.capital11.api;

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

import com.capital11.web.SessionKeys;

class ApiConfigTest {

    private InterceptorRegistration registration;
    private HandlerInterceptor interceptor;

    @BeforeEach
    void registerInterceptor() {
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        registration = mock(InterceptorRegistration.class, RETURNS_SELF);
        when(registry.addInterceptor(any())).thenReturn(registration);

        new ApiConfig().addInterceptors(registry);

        ArgumentCaptor<HandlerInterceptor> captor = ArgumentCaptor.forClass(HandlerInterceptor.class);
        verify(registry).addInterceptor(captor.capture());
        interceptor = captor.getValue();
    }

    @Test
    void guardsOnlyTheMeEndpoints() {
        verify(registration).addPathPatterns("/api/me", "/api/me/**");
    }

    @Test
    void letsLoggedInCustomerThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.CUSTOMER_ID, 4242);
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void answersUnauthorizedInsteadOfRedirectingWithoutSession() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(new MockHttpServletRequest(), response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    void answersUnauthorizedWhenSessionHasNoCustomer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }
}
