package com.ms.utils.mockpit.auth.http;

import com.ms.utils.mockpit.config.MockpitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafeHttpClientTest {

    private SafeHttpClient client;
    private MockpitProperties props;

    @BeforeEach
    void setup() {
        props = new MockpitProperties();
        props.getHttpClient().setAllowedHosts(Collections.emptyList());
        client = new SafeHttpClient();
        ReflectionTestUtils.setField(client, "properties", props);
    }

    @Test
    void rejectsFileScheme() {
        assertThatThrownBy(() -> client.getJson("file:///etc/passwd"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Only http/https");
    }

    @Test
    void rejectsLoopback() {
        // localhost resolves to 127.0.0.1 / ::1 which are loopback - blocked.
        assertThatThrownBy(() -> client.getJson("http://localhost:1/.well-known/jwks.json"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("forbidden");
    }

    @Test
    void rejectsCloudMetadataIp() {
        assertThatThrownBy(() -> client.getJson("http://169.254.169.254/latest/meta-data/"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("forbidden");
    }

    @Test
    void rejectsRfc1918() {
        assertThatThrownBy(() -> client.getJson("http://10.0.0.1/jwks"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("forbidden");
    }

    @Test
    void rejectsHostNotInAllowlistWhenSet() {
        props.getHttpClient().setAllowedHosts(List.of("issuer.example.com"));
        assertThatThrownBy(() -> client.getJson("https://attacker.example.com/jwks"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("allowlist");
    }
}
