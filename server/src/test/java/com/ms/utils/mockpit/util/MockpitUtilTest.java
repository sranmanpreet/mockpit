package com.ms.utils.mockpit.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MockpitUtilTest {

    @Test
    void exactPathMatches() {
        assertThat(MockpitUtil.isMatch("/api/users", "/api/users")).isTrue();
    }

    @Test
    void pathVariableMatches() {
        assertThat(MockpitUtil.isMatch("/api/users/42", "/api/users/:id")).isTrue();
    }

    @Test
    void queryParametersAreStripped() {
        assertThat(MockpitUtil.isMatch("/api/users?foo=bar", "/api/users")).isTrue();
    }

    @Test
    void mismatchedPaths() {
        assertThat(MockpitUtil.isMatch("/api/orders", "/api/users")).isFalse();
    }

    @Test
    void extractsPathVariableValues() {
        Map<String, String> vars = MockpitUtil.getPathVariableMap("/api/users/42", "/api/users/:id");
        assertThat(vars).containsEntry("id", "42");
    }

    @Test
    void multiplePathVariables() {
        Map<String, String> vars = MockpitUtil.getPathVariableMap(
                "/api/users/42/posts/7", "/api/users/:userId/posts/:postId");
        assertThat(vars).containsEntry("userId", "42").containsEntry("postId", "7");
    }
}
