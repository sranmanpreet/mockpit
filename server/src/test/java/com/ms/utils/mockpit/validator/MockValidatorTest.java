package com.ms.utils.mockpit.validator;

import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.dto.ResponseBodyDTO;
import com.ms.utils.mockpit.dto.ResponseStatusDTO;
import com.ms.utils.mockpit.dto.RouteDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockValidatorTest {

    private final MockValidator validator = new MockValidator();

    private MockDTO valid() {
        MockDTO m = new MockDTO();
        m.setName("ok");
        RouteDTO r = new RouteDTO();
        r.setPath("/foo");
        r.setMethod(com.ms.utils.mockpit.enums.Method.GET);
        m.setRoute(r);
        ResponseBodyDTO rb = new ResponseBodyDTO();
        rb.setContentType("application/json");
        rb.setContent("{}");
        m.setResponseBody(rb);
        ResponseStatusDTO rs = new ResponseStatusDTO();
        rs.setCode(200);
        m.setResponseStatus(rs);
        return m;
    }

    @Test
    void acceptsAValidMock() throws Exception {
        assertThat(validator.isMockValid(valid())).isTrue();
    }

    @Test
    void rejectsNullName() {
        MockDTO m = valid();
        m.setName(null);
        assertThatThrownBy(() -> validator.isMockValid(m))
                .isInstanceOf(MockpitApplicationException.class);
    }

    @Test
    void rejectsReservedRoutePath() {
        MockDTO m = valid();
        m.getRoute().setPath("/native/api/mocks");
        assertThatThrownBy(() -> validator.isMockValid(m))
                .isInstanceOf(MockpitApplicationException.class)
                .hasMessageContaining("reserved");
    }

    @Test
    void rejectsActuatorPath() {
        MockDTO m = valid();
        m.getRoute().setPath("/actuator/health");
        assertThatThrownBy(() -> validator.isMockValid(m))
                .isInstanceOf(MockpitApplicationException.class)
                .hasMessageContaining("reserved");
    }

    @Test
    void rejectsTooLongName() {
        MockDTO m = valid();
        m.setName("x".repeat(300));
        assertThatThrownBy(() -> validator.isMockValid(m))
                .isInstanceOf(MockpitApplicationException.class)
                .hasMessageContaining("name is too long");
    }
}
