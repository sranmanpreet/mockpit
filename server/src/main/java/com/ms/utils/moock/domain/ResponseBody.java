package com.ms.utils.moock.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.MediaType;

import javax.persistence.*;

@Entity
@Table(name = "response_body")
public class ResponseBody extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @OneToOne(targetEntity = Mock.class)
    @JoinColumn(name = "mock")
    private Mock mock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Mock getMock() {
        return mock;
    }

    public void setMock(Mock mock) {
        this.mock = mock;
    }
}
