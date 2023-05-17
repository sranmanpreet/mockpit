package com.ms.utils.mockbuddy.domain;

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

    @OneToOne(targetEntity = Mock.class)
    @JoinColumn(name = "mock_id")
    private Long mockId;

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

    public Long getMockId() {
        return mockId;
    }

    public void setMockId(Long mockId) {
        this.mockId = mockId;
    }
}
