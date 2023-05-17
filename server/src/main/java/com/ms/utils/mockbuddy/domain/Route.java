package com.ms.utils.mockbuddy.domain;

import javax.persistence.*;

@Entity
@Table(name = "route")
public class Route extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "path", nullable = false)
    private String path;

    @OneToOne(targetEntity = Mock.class)
    @JoinColumn(name = "mock_id")
    private Long mockId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getMockId() {
        return mockId;
    }

    public void setMockId(Long mockId) {
        this.mockId = mockId;
    }
}
