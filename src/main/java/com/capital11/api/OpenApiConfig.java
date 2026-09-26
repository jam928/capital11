package com.capital11.api;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Capital 11 Bank API", version = "v1",
        description = "Log in with POST /api/session; the session cookie it sets authenticates the /api/me endpoints."))
@SecurityScheme(name = OpenApiConfig.SESSION_COOKIE, type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE, paramName = "JSESSIONID")
class OpenApiConfig {

    static final String SESSION_COOKIE = "sessionCookie";
}
