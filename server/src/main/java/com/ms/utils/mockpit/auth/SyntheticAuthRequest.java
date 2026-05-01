package com.ms.utils.mockpit.auth;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.MappingMatch;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.PushBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal {@link HttpServletRequest} implementation used by
 * {@code MockResource#testAuth} to feed a synthetic request into an {@link AuthValidator} without
 * pulling spring-test (which is test scope only) into the runtime classpath. Only the methods
 * actually called by validators are implemented; everything else is a safe no-op.
 *
 * <p>Targets the Servlet 6.0 API (Jakarta EE 10).
 */
public class SyntheticAuthRequest implements HttpServletRequest {

    private final String method;
    private final String uri;
    private final Map<String, String> headers = new LinkedHashMap<>();

    public SyntheticAuthRequest(String method, String uri, Map<String, String> headers) {
        this.method = method == null ? "GET" : method;
        this.uri = uri == null ? "/" : uri;
        if (headers != null) {
            headers.forEach((k, v) -> { if (k != null && v != null) this.headers.put(k, v); });
        }
    }

    @Override public String getHeader(String name) {
        if (name == null) return null;
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name)) return e.getValue();
        }
        return null;
    }
    @Override public Enumeration<String> getHeaders(String name) {
        String v = getHeader(name);
        return Collections.enumeration(v == null ? Collections.emptyList() : Collections.singletonList(v));
    }
    @Override public Enumeration<String> getHeaderNames() { return Collections.enumeration(headers.keySet()); }
    @Override public String getMethod() { return method; }
    @Override public String getRequestURI() { return uri; }

    // ------------------------ Unsupported / no-op below ------------------------

    @Override public String getAuthType() { return null; }
    @Override public Cookie[] getCookies() { return new Cookie[0]; }
    @Override public long getDateHeader(String name) { return -1; }
    @Override public int getIntHeader(String name) { return -1; }
    @Override public String getPathInfo() { return null; }
    @Override public String getPathTranslated() { return null; }
    @Override public String getContextPath() { return ""; }
    @Override public String getQueryString() { return null; }
    @Override public String getRemoteUser() { return null; }
    @Override public boolean isUserInRole(String role) { return false; }
    @Override public Principal getUserPrincipal() { return null; }
    @Override public String getRequestedSessionId() { return null; }
    @Override public StringBuffer getRequestURL() { return new StringBuffer(uri); }
    @Override public String getServletPath() { return uri; }
    @Override public HttpSession getSession(boolean create) { return null; }
    @Override public HttpSession getSession() { return null; }
    @Override public String changeSessionId() { return null; }
    @Override public boolean isRequestedSessionIdValid() { return false; }
    @Override public boolean isRequestedSessionIdFromCookie() { return false; }
    @Override public boolean isRequestedSessionIdFromURL() { return false; }
    @Override public boolean authenticate(HttpServletResponse response) { return false; }
    @Override public void login(String username, String password) { }
    @Override public void logout() { }
    @Override public Collection<Part> getParts() { return Collections.emptyList(); }
    @Override public Part getPart(String name) { return null; }
    @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
    @Override public Object getAttribute(String name) { return null; }
    @Override public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
    @Override public String getCharacterEncoding() { return "UTF-8"; }
    @Override public void setCharacterEncoding(String env) { }
    @Override public int getContentLength() { return 0; }
    @Override public long getContentLengthLong() { return 0; }
    @Override public String getContentType() { return null; }
    @Override public ServletInputStream getInputStream() throws IOException { return null; }
    @Override public String getParameter(String name) { return null; }
    @Override public Enumeration<String> getParameterNames() { return Collections.emptyEnumeration(); }
    @Override public String[] getParameterValues(String name) { return new String[0]; }
    @Override public Map<String, String[]> getParameterMap() { return Collections.emptyMap(); }
    @Override public String getProtocol() { return "HTTP/1.1"; }
    @Override public String getScheme() { return "http"; }
    @Override public String getServerName() { return "localhost"; }
    @Override public int getServerPort() { return 80; }
    @Override public BufferedReader getReader() { return null; }
    @Override public String getRemoteAddr() { return "127.0.0.1"; }
    @Override public String getRemoteHost() { return "localhost"; }
    @Override public void setAttribute(String name, Object o) { }
    @Override public void removeAttribute(String name) { }
    @Override public Locale getLocale() { return Locale.getDefault(); }
    @Override public Enumeration<Locale> getLocales() { return Collections.enumeration(Collections.singletonList(Locale.getDefault())); }
    @Override public boolean isSecure() { return false; }
    @Override public RequestDispatcher getRequestDispatcher(String path) { return null; }
    @Override public int getRemotePort() { return 0; }
    @Override public String getLocalName() { return null; }
    @Override public String getLocalAddr() { return null; }
    @Override public int getLocalPort() { return 0; }
    @Override public ServletContext getServletContext() { return null; }
    @Override public AsyncContext startAsync() { return null; }
    @Override public AsyncContext startAsync(ServletRequest request, ServletResponse response) { return null; }
    @Override public boolean isAsyncStarted() { return false; }
    @Override public boolean isAsyncSupported() { return false; }
    @Override public AsyncContext getAsyncContext() { return null; }
    @Override public DispatcherType getDispatcherType() { return DispatcherType.REQUEST; }

    // ---------- Servlet 6.0 additions ----------
    @Override public String getRequestId() { return ""; }
    @Override public String getProtocolRequestId() { return ""; }
    @Override public ServletConnection getServletConnection() {
        return new ServletConnection() {
            @Override public String getConnectionId() { return ""; }
            @Override public String getProtocol() { return "HTTP/1.1"; }
            @Override public String getProtocolConnectionId() { return ""; }
            @Override public boolean isSecure() { return false; }
        };
    }
    @Override public PushBuilder newPushBuilder() { return null; }
    @Override public HttpServletMapping getHttpServletMapping() {
        return new HttpServletMapping() {
            @Override public String getMatchValue() { return ""; }
            @Override public String getPattern() { return ""; }
            @Override public String getServletName() { return ""; }
            @Override public MappingMatch getMappingMatch() { return MappingMatch.DEFAULT; }
        };
    }
}
