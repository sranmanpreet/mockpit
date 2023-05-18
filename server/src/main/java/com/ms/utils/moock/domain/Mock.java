package com.ms.utils.moock.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @OneToOne(mappedBy = "mock", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Route route;

    @OneToMany(mappedBy = "mock", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ResponseHeader> responseHeaders = new HashSet<>();

    @OneToOne(mappedBy = "mock", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private ResponseBody responseBody;

    @OneToOne(mappedBy = "mock", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private ResponseStatus responseStatus;

    public Mock(){}

    public Mock(String name, String description){
        this.name = name;
        this.description = description;
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
