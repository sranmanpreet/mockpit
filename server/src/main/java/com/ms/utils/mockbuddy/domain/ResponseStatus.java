package com.ms.utils.mockbuddy.domain;

import javax.persistence.*;

@Entity
@Table(name = "response_status")
public class ResponseStatus extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private int code;

    @OneToOne(targetEntity = Mock.class)
    @JoinColumn(name = "mock_id")
    private Long mockId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Long getMockId() {
        return mockId;
    }

    public void setMockId(Long mockId) {
        this.mockId = mockId;
    }
}

