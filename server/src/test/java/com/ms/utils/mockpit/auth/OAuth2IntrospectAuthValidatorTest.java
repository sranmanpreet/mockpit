package com.ms.utils.mockpit.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.ms.utils.mockpit.auth.config.OAuth2IntrospectAuthConfig;
import com.ms.utils.mockpit.auth.http.SafeHttpClient;
import com.ms.utils.mockpit.config.MockpitProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

class OAuth2IntrospectAuthValidatorTest {

    private static WireMockServer wireMock;
    private OAuth2IntrospectAuthValidator validator;
    private MockpitProperties props;

    @BeforeAll
    static void start() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stop() {
        wireMock.stop();
    }

    @BeforeEach
    void setup() {
        wireMock.resetAll();
        props = new MockpitProperties();
        props.getHttpClient().setAllowedHosts(List.of("localhost"));
        // Tests need to talk to WireMock-on-localhost; production blocks loopback.
        props.getHttpClient().setAllowLoopback(true);
        SafeHttpClient httpClient = new SafeHttpClient();
        ReflectionTestUtils.setField(httpClient, "properties", props);
        validator = new OAuth2IntrospectAuthValidator();
        ReflectionTestUtils.setField(validator, "httpClient", httpClient);
        ReflectionTestUtils.setField(validator, "objectMapper", new ObjectMapper());
    }

    private OAuth2IntrospectAuthConfig cfg() {
        OAuth2IntrospectAuthConfig c = new OAuth2IntrospectAuthConfig();
        c.setIntrospectionUri("http://localhost:" + wireMock.port() + "/introspect");
        c.setClientId("client");
        c.setClientSecret("secret");
        return c;
    }

    private MockHttpServletRequest req(String token) {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.addHeader("Authorization", "Bearer " + token);
        return r;
    }

    @Test
    void successOnActiveTrue() {
        wireMock.stubFor(post(urlEqualTo("/introspect"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"active\": true, \"scope\": \"read write\"}")));
        AuthValidationResult r = validator.validate(req("opaque-token"), cfg());
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    void failureOnActiveFalse() {
        wireMock.stubFor(post(urlEqualTo("/introspect"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"active\": false}")));
        AuthValidationResult r = validator.validate(req("opaque-token"), cfg());
        assertThat(r.isFailure()).isTrue();
    }

    @Test
    void failureOnIntrospectionHttpError() {
        wireMock.stubFor(post(urlEqualTo("/introspect"))
                .willReturn(aResponse().withStatus(500)));
        AuthValidationResult r = validator.validate(req("opaque-token"), cfg());
        assertThat(r.isFailure()).isTrue();
    }

    @Test
    void rejectsMissingBearer() {
        AuthValidationResult r = validator.validate(new MockHttpServletRequest(), cfg());
        assertThat(r.isFailure()).isTrue();
    }

    @Test
    void enforcesScopeWhenConfigured() {
        wireMock.stubFor(post(urlEqualTo("/introspect"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"active\": true, \"scope\": \"read\"}")));
        OAuth2IntrospectAuthConfig c = cfg();
        c.setRequiredScopes(List.of("admin"));
        AuthValidationResult r = validator.validate(req("opaque-token"), c);
        assertThat(r.isFailure()).isTrue();
        assertThat(r.getWwwAuthenticate()).contains("insufficient_scope");
    }
}
