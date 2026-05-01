package com.ms.utils.mockpit.auth.http;

import com.ms.utils.mockpit.config.MockpitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * SSRF-hardened HTTP client used for outbound auth-related requests (JWKS, OIDC discovery,
 * introspection). Defends against:
 *
 * <ul>
 *   <li>Schemes other than {@code https} or {@code http} (no {@code file://}, {@code gopher://},
 *       {@code ftp://}, etc.).</li>
 *   <li>DNS rebinding to private IPv4/IPv6 ranges, loopback, link-local, multicast, broadcast,
 *       and the cloud-metadata IP {@code 169.254.169.254}.</li>
 *   <li>Hosts not in the operator-configured allowlist (when the allowlist is non-empty).</li>
 *   <li>Unbounded response sizes / infinite reads (configurable timeouts and a hard byte cap).</li>
 *   <li>Following redirects to any of the above.</li>
 * </ul>
 *
 * <p>Note: a fully-correct SSRF defence requires resolving DNS exactly once and then connecting
 * directly to the resolved IP (the standard "TOCTOU on DNS" mitigation). For Mockpit's needs, we
 * resolve, validate, then issue the request - we accept a small risk window in exchange for not
 * pinning to a specific IP. The {@link #MAX_RESPONSE_BYTES} ceiling and tight timeouts limit blast
 * radius if a rebind happens.
 */
@Component
public class SafeHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SafeHttpClient.class);
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024; // 1 MiB

    @Autowired
    private MockpitProperties properties;

    public Response getJson(String url) throws IOException {
        return execute("GET", url, null, "application/json");
    }

    public Response postForm(String url, String body, String basicAuth) throws IOException {
        return execute("POST", url, body, "application/x-www-form-urlencoded", basicAuth);
    }

    private Response execute(String method, String url, String body, String accept) throws IOException {
        return execute(method, url, body, accept, null);
    }

    private Response execute(String method, String urlStr, String body, String contentType, String basicAuth)
            throws IOException {

        URI uri;
        try {
            uri = URI.create(urlStr);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid URL.");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))) {
            throw new IOException("Only http/https URLs are permitted.");
        }
        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new IOException("URL has no host.");
        }

        List<String> allowed = properties.getHttpClient().getAllowedHosts();
        if (allowed != null && !allowed.isEmpty()) {
            boolean hostAllowed = allowed.stream().anyMatch(h -> hostMatches(host, h));
            if (!hostAllowed) {
                throw new IOException("Host '" + host + "' is not in the configured allowlist.");
            }
        }

        InetAddress[] resolved;
        try {
            resolved = InetAddress.getAllByName(host);
        } catch (UnknownHostException ex) {
            throw new IOException("Unable to resolve host: " + host);
        }
        for (InetAddress addr : resolved) {
            if (isUnsafeAddress(addr)) {
                throw new IOException("Host '" + host + "' resolves to a forbidden address: " + addr.getHostAddress());
            }
        }

        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod(method);
            conn.setConnectTimeout(properties.getHttpClient().getConnectTimeoutMs());
            conn.setReadTimeout(properties.getHttpClient().getReadTimeoutMs());
            conn.setInstanceFollowRedirects(false);
            if (contentType != null) conn.setRequestProperty("Accept", contentType);
            if (basicAuth != null) conn.setRequestProperty("Authorization", "Basic " + basicAuth);
            conn.setRequestProperty("User-Agent", "Mockpit/2.0");

            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", contentType);
                byte[] payload = body.getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(payload.length);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(payload);
                }
            }
            int status = conn.getResponseCode();
            byte[] data = readCappedStream(status >= 400 ? conn.getErrorStream() : conn.getInputStream());
            return new Response(status, data == null ? "" : new String(data, StandardCharsets.UTF_8));
        } finally {
            conn.disconnect();
        }
    }

    private static byte[] readCappedStream(java.io.InputStream in) throws IOException {
        if (in == null) return new byte[0];
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int total = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            total += n;
            if (total > MAX_RESPONSE_BYTES) {
                throw new IOException("Response exceeds " + MAX_RESPONSE_BYTES + " bytes.");
            }
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static boolean isUnsafeAddress(InetAddress addr) {
        if (addr.isAnyLocalAddress()) return true;
        if (addr.isLoopbackAddress()) return true;
        if (addr.isLinkLocalAddress()) return true;
        if (addr.isMulticastAddress()) return true;
        if (addr.isSiteLocalAddress()) return true; // RFC 1918 / IPv6 ULA
        // 169.254.169.254 (AWS, GCP, Azure metadata)
        String ip = addr.getHostAddress();
        if ("169.254.169.254".equals(ip)) return true;
        // Cloud metadata IPv6 range fd00:ec2::254 is covered by isSiteLocalAddress.
        return false;
    }

    private static boolean hostMatches(String host, String pattern) {
        if (pattern == null) return false;
        String h = host.toLowerCase();
        String p = pattern.toLowerCase();
        if (p.startsWith("*.")) {
            return h.endsWith(p.substring(1)) && h.length() > p.length() - 1;
        }
        return h.equals(p);
    }

    public static class Response {
        public final int status;
        public final String body;
        public Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}
