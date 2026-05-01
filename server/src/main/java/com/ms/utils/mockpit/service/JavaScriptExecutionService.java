package com.ms.utils.mockpit.service;

import com.ms.utils.mockpit.config.MockpitProperties;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Sandboxed JavaScript execution for dynamic mock response bodies.
 *
 * <p>Hardening applied (vs the pre-2.0 implementation):
 * <ul>
 *   <li>Shared {@link Engine} so per-request compilation is cached.</li>
 *   <li>{@link HostAccess#NONE} - user code cannot reach into Java objects.</li>
 *   <li>{@link PolyglotAccess#NONE} - no language hopping.</li>
 *   <li>{@code allowIO=false}, {@code allowCreateThread=false}, {@code allowNativeAccess=false},
 *       {@code allowHostClassLoading=false}.</li>
 *   <li>Statement count limit via the GraalJS {@code engine.MaxStatements} option.</li>
 *   <li>Wall-clock timeout enforced by a watchdog calling {@code Context.close(true)}.</li>
 *   <li>Output truncated to the configured byte cap to prevent OOM via response inflation.</li>
 *   <li>Bindings are limited to the explicit query / path variable map - no globals leak.</li>
 * </ul>
 */
@Service
public class JavaScriptExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptExecutionService.class);
    private static final String LANG = "js";

    private final Engine engine;
    private final ScheduledExecutorService watchdog;
    private final MockpitProperties properties;
    private final Environment env;
    private volatile boolean sandboxLimitsSupported = true;

    @Autowired
    public JavaScriptExecutionService(MockpitProperties properties, Environment env) {
        this.properties = properties;
        this.env = env;
        this.engine = Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", "false")
                .build();
        this.watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mockpit-js-watchdog");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    public void shutdown() {
        watchdog.shutdownNow();
        engine.close();
    }

    /**
     * Result of a sandboxed evaluation.
     */
    public static class JsResult {
        private final boolean success;
        private final String output;
        private final String error;

        private JsResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }

        public static JsResult ok(String output) { return new JsResult(true, output, null); }
        public static JsResult fail(String error) { return new JsResult(false, "", error); }

        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public String getError() { return error; }
    }

    /**
     * Backwards-compatible signature that returns the executed output as an Object so existing
     * callers continue to work. New callers should prefer {@link #executeSafely(Object, Map, Map)}.
     */
    public Object execute(Object snippet, Map<String, String[]> queryParams, Map<String, String> pathVariables) {
        JsResult result = executeSafely(snippet, queryParams, pathVariables);
        return result.isSuccess() ? result.getOutput() : "Error: " + result.getError();
    }

    public JsResult executeSafely(Object snippet,
                                  Map<String, String[]> queryParams,
                                  Map<String, String> pathVariables) {
        if (snippet == null) {
            return JsResult.fail("No script provided.");
        }
        long timeoutMs = properties.getJsSandbox().getTimeoutMs();
        long maxStatements = properties.getJsSandbox().getMaxStatements();
        int maxBytes = properties.getJsSandbox().getMaxOutputBytes();

        // GraalVM 24.x community does NOT support sandbox.* / engine.MaxStatements options
        // (those are enterprise extensions). The wall-clock watchdog below remains the primary
        // safety net for runaway scripts. {@code maxStatements} is therefore only effective when
        // running on a GraalVM Enterprise runtime; we ignore the field on community.
        Context.Builder builder = Context.newBuilder(LANG)
                .engine(engine)
                .allowAllAccess(false)
                .allowHostAccess(HostAccess.NONE)
                .allowPolyglotAccess(PolyglotAccess.NONE)
                .allowIO(IOAccess.NONE)
                .allowNativeAccess(false)
                .allowCreateThread(false)
                .allowHostClassLoading(false)
                .allowHostClassLookup(s -> false)
                .allowExperimentalOptions(true)
                .option("js.ecmascript-version", "2022")
                .option("js.console", "false")
                .option("js.load", "false")
                .option("js.print", "false");

        if (maxStatements > 0 && sandboxLimitsSupported) {
            builder.option("sandbox.MaxStatements", String.valueOf(maxStatements));
        }

        try (Context context = openContext(builder)) {
            String queryParamPrefix = orDefault(env.getProperty("dynamic-mocks.query-parameter-prefix"), "queryParameter___");
            String pathVariablePrefix = orDefault(env.getProperty("dynamic-mocks.path-variable-prefix"), "pathVariable___");

            Value bindings = context.getBindings(LANG);
            if (queryParams != null) {
                queryParams.forEach((k, v) -> {
                    if (v != null && v.length > 0) {
                        bindings.putMember(queryParamPrefix + k, v[0]);
                    }
                });
            }
            if (pathVariables != null) {
                pathVariables.forEach((k, v) -> bindings.putMember(pathVariablePrefix + k, v));
            }

            ScheduledFuture<?> killer = watchdog.schedule(
                    () -> context.close(true),
                    timeoutMs,
                    TimeUnit.MILLISECONDS);

            try {
                Source src = Source.newBuilder(LANG, "(() => { " + snippet + " })();", "mock-script.js").build();
                Value result = context.eval(src);
                String out = result == null ? "" : String.valueOf(result);
                if (out.length() > maxBytes) {
                    LOGGER.warn("Sandboxed JS output exceeded {} bytes; truncating.", maxBytes);
                    out = out.substring(0, maxBytes);
                }
                return JsResult.ok(out);
            } finally {
                killer.cancel(false);
            }
        } catch (Exception e) {
            String safeMsg = e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : ": " + e.getMessage().split("\\R")[0]);
            LOGGER.warn("JavaScript execution failed: {}", safeMsg);
            return JsResult.fail(safeMsg);
        }
    }

    /**
     * Build a Context, falling back to a no-sandbox-limits builder if the runtime rejects the
     * sandbox option (GraalVM community vs. enterprise). The community runtime throws
     * {@code PolyglotException}, the enterprise/native one throws {@code IllegalArgumentException}
     * for unknown options - we accept either.
     */
    private Context openContext(Context.Builder primary) {
        try {
            return primary.build();
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage();
            if (!msg.contains("sandbox") && !(ex instanceof IllegalArgumentException)) {
                throw ex;
            }
            sandboxLimitsSupported = false;
            LOGGER.info("GraalVM runtime does not support sandbox limits ({}); falling back to wall-clock timeout only.",
                    msg.isEmpty() ? ex.getClass().getSimpleName() : msg.split("\\R")[0]);
            return Context.newBuilder(LANG)
                    .engine(engine)
                    .allowAllAccess(false)
                    .allowHostAccess(HostAccess.NONE)
                    .allowPolyglotAccess(PolyglotAccess.NONE)
                    .allowIO(IOAccess.NONE)
                    .allowNativeAccess(false)
                    .allowCreateThread(false)
                    .allowHostClassLoading(false)
                    .allowHostClassLookup(s -> false)
                    .allowExperimentalOptions(true)
                    .option("js.ecmascript-version", "2022")
                    .option("js.console", "false")
                    .option("js.load", "false")
                    .option("js.print", "false")
                    .build();
        }
    }

    private static String orDefault(String v, String fallback) {
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    public Engine getEngine() {
        return engine;
    }

    public boolean isShutdown() {
        return Objects.isNull(engine) || watchdog.isShutdown();
    }
}
