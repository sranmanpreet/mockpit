package com.ms.utils.mockbuddy.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "mock")
public class Mock extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToOne(mappedBy = "mockId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Route route;

    @OneToMany(mappedBy = "mockId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResponseHeader> responseHeaders = new HashSet<>();

    @OneToOne(mappedBy = "mockId", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResponseBody responseBody;

    @OneToOne(mappedBy = "mockId", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResponseStatus responseStatus;

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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Set<ResponseHeader> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Set<ResponseHeader> responseHeaders) {
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
}
