package ru.itmo.tpo.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AirStateTest {

    @Test
    void shouldIntensifyRawAirFromDepths() {
        AirState airState = new AirState(0.2, 0.3);

        airState.intensifyFromDepths(0.4);

        assertTrue(airState.rawness() > 0.2);
        assertTrue(airState.flowFromDepths() > 0.3);
    }

    @Test
    void shouldClampValuesIntoRange() {
        AirState airState = new AirState(2.0, -1.0);

        assertEquals(1.0, airState.rawness(), 1e-9);
        assertEquals(0.0, airState.flowFromDepths(), 1e-9);

        airState.intensifyFromDepths(10.0);
        assertEquals(1.0, airState.rawness(), 1e-9);
        assertEquals(1.0, airState.flowFromDepths(), 1e-9);
    }

    @Test
    void shouldRejectNegativeDelta() {
        AirState airState = new AirState(0.2, 0.2);
        assertThrows(IllegalArgumentException.class, () -> airState.intensifyFromDepths(-0.1));
    }
}
