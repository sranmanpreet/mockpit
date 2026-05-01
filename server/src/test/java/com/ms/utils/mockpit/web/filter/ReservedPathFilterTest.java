package com.ms.utils.mockpit.web.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservedPathFilterTest {

    @Test
    void recognisesAllPrefixes() {
        assertThat(ReservedPathFilter.isReservedPath("/native/api/mocks")).isTrue();
        assertThat(ReservedPathFilter.isReservedPath("/auth/login")).isTrue();
        assertThat(ReservedPathFilter.isReservedPath("/actuator/health")).isTrue();
        assertThat(ReservedPathFilter.isReservedPath("/swagger-ui.html")).isTrue();
        assertThat(ReservedPathFilter.isReservedPath("/v3/api-docs")).isTrue();
        assertThat(ReservedPathFilter.isReservedPath("/error")).isTrue();
    }

    @Test
    void allowsUserPaths() {
        assertThat(ReservedPathFilter.isReservedPath("/api/users/42")).isFalse();
        assertThat(ReservedPathFilter.isReservedPath("/")).isFalse();
        assertThat(ReservedPathFilter.isReservedPath(null)).isFalse();
    }
}
