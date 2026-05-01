package com.ms.utils.mockpit.service;

import com.ms.utils.mockpit.config.MockpitProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JavaScriptExecutionServiceTest {

    private JavaScriptExecutionService svc;

    @BeforeEach
    void setup() {
        MockpitProperties props = new MockpitProperties();
        props.getJsSandbox().setTimeoutMs(500);
        props.getJsSandbox().setMaxStatements(50_000);
        props.getJsSandbox().setMaxOutputBytes(64 * 1024);
        svc = new JavaScriptExecutionService(props, new MockEnvironment());
    }

    @AfterEach
    void teardown() {
        svc.shutdown();
    }

    @Test
    void evaluatesSimpleReturn() {
        JavaScriptExecutionService.JsResult r = svc.executeSafely(
                "return JSON.stringify({hello: 'world'});",
                Collections.emptyMap(), Collections.emptyMap());
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getOutput()).contains("hello").contains("world");
    }

    @Test
    void exposesQueryParamsAndPathVariablesViaPrefixes() {
        Map<String, String[]> qp = new HashMap<>();
        qp.put("name", new String[]{"alice"});
        Map<String, String> pv = new HashMap<>();
        pv.put("id", "42");
        JavaScriptExecutionService.JsResult r = svc.executeSafely(
                "return queryParameter___name + ':' + pathVariable___id;", qp, pv);
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getOutput()).isEqualTo("alice:42");
    }

    @Test
    void infiniteLoopIsKilledByWatchdog() {
        long start = System.currentTimeMillis();
        JavaScriptExecutionService.JsResult r = svc.executeSafely(
                "while(true){};", Collections.emptyMap(), Collections.emptyMap());
        long elapsed = System.currentTimeMillis() - start;
        assertThat(r.isSuccess()).isFalse();
        // Bounded by watchdog timeout (500ms) + some slack.
        assertThat(elapsed).isLessThan(5_000L);
    }

    @Test
    void cannotAccessJavaHostObjects() {
        // With HostAccess.NONE the user cannot reach into Java types.
        JavaScriptExecutionService.JsResult r = svc.executeSafely(
                "try { return Java.type('java.lang.Runtime').getRuntime().exec(['id']).toString(); } catch(e) { return 'blocked'; }",
                Collections.emptyMap(), Collections.emptyMap());
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getOutput()).isEqualTo("blocked");
    }

    @Test
    void cannotAccessFilesystem() {
        JavaScriptExecutionService.JsResult r = svc.executeSafely(
                "try { load('/etc/passwd'); return 'loaded'; } catch(e) { return 'blocked'; }",
                Collections.emptyMap(), Collections.emptyMap());
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getOutput()).isEqualTo("blocked");
    }

    @Test
    void truncatesLargeOutput() {
        JavaScriptExecutionService.JsResult r = svc.executeSafely(
                "return 'x'.repeat(200000);",
                Collections.emptyMap(), Collections.emptyMap());
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getOutput().length()).isLessThanOrEqualTo(64 * 1024);
    }
}
