package com.capital11.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.capital11.web.SessionKeys;

@Configuration
class ApiConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiLoginInterceptor()).addPathPatterns("/api/me", "/api/me/**");
    }

    /** Like the page LoginInterceptor, but answers 401 instead of redirecting to the login page. */
    private static class ApiLoginInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(SessionKeys.CUSTOMER_ID) != null) {
                return true;
            }
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }
}
