package com.ms.utils.mockpit.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock")
public class Mock extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private boolean inactive;

    /**
     * Owner of the mock. Nullable for legacy (pre-2.0) mocks created before multi-tenancy
     * existed - those are visible to admins only and need to be re-assigned manually.
     */
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 32)
    private AuthType authType = AuthType.NONE;

    /**
     * JSON payload describing the auth configuration. Schema depends on {@link #authType}.
     * Secrets inside this payload are encrypted at rest by {@code SecretCipher} before save and
     * decrypted on load.
     */
    @Column(name = "auth_config_json", columnDefinition = "text")
    private String authConfigJson;

    @Column(name = "auth_failure_status")
    private Integer authFailureStatus;

    @Column(name = "auth_failure_body", columnDefinition = "text")
    private String authFailureBody;

    @Column(name = "auth_failure_content_type", length = 120)
    private String authFailureContentType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_id")
    private List<ResponseHeader> responseHeaders = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "response_body_id")
    private ResponseBody responseBody;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "response_status_id")
    private ResponseStatus responseStatus;

    public Mock(){}

    public Mock(String name, String description){
        this.name = name;
        this.description = description;
    }

    public Mock(String name, String description, boolean inactive){
        this.name = name;
        this.description = description;
        this.inactive = inactive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getInactive() { return inactive; }

    public void setInactive(boolean inactive) { this.inactive = inactive;}

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public List<ResponseHeader> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(List<ResponseHeader> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public ResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public AuthType getAuthType() { return authType == null ? AuthType.NONE : authType; }
    public void setAuthType(AuthType authType) { this.authType = authType == null ? AuthType.NONE : authType; }
    public String getAuthConfigJson() { return authConfigJson; }
    public void setAuthConfigJson(String authConfigJson) { this.authConfigJson = authConfigJson; }
    public Integer getAuthFailureStatus() { return authFailureStatus; }
    public void setAuthFailureStatus(Integer authFailureStatus) { this.authFailureStatus = authFailureStatus; }
    public String getAuthFailureBody() { return authFailureBody; }
    public void setAuthFailureBody(String authFailureBody) { this.authFailureBody = authFailureBody; }
    public String getAuthFailureContentType() { return authFailureContentType; }
    public void setAuthFailureContentType(String authFailureContentType) { this.authFailureContentType = authFailureContentType; }
}
