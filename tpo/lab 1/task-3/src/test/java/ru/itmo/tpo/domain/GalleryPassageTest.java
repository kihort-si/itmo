package ru.itmo.tpo.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class GalleryPassageTest {

    @Test
    void shouldMoveThroughPassageStates() {
        GalleryPassage passage = new GalleryPassage(
                "A-B",
                new Blockage(BlockageMaterial.RUBBLE, 10.0),
                0.7,
                0.9
        );

        assertEquals(PassageState.BLOCKED, passage.state());
        passage.clearBy(new Zaphod(), Duration.ofSeconds(1));
        assertEquals(PassageState.PARTIALLY_CLEARED, passage.state());

        passage.clearBy(new Marvin(), Duration.ofSeconds(2));
        assertEquals(PassageState.PASSABLE, passage.state());
        assertTrue(passage.isPassable());
    }

    @Test
    void marvinShouldClearFasterThanZaphod() {
        GalleryPassage p1 = new GalleryPassage("P1", new Blockage(BlockageMaterial.RUBBLE, 20.0), 0.4, 0.8);
        GalleryPassage p2 = new GalleryPassage("P2", new Blockage(BlockageMaterial.RUBBLE, 20.0), 0.4, 0.8);

        p1.clearBy(new Zaphod(), Duration.ofSeconds(2));
        p2.clearBy(new Marvin(), Duration.ofSeconds(2));

        assertTrue(p2.blockage().remainingMass() < p1.blockage().remainingMass());
    }

    @Test
    void shouldComputeVisibilityWithDustAndRawAir() {
        GalleryPassage passage = new GalleryPassage("A-B", new Blockage(BlockageMaterial.RUBBLE, 10.0), 0.2, 0.9);
        AirState lowRaw = new AirState(0.1, 0.1);
        AirState highRaw = new AirState(0.9, 0.9);
        Flashlight flashlight = new Flashlight(1.0);

        double betterVisibility = passage.visibility(flashlight, lowRaw);
        double worseVisibility = passage.visibility(flashlight, highRaw);

        assertTrue(betterVisibility > worseVisibility);
    }

    @Test
    void shouldRejectInvalidArguments() {
        GalleryPassage passage = new GalleryPassage("A-B", new Blockage(BlockageMaterial.RUBBLE, 10.0), 0.4, 0.9);

        assertThrows(IllegalArgumentException.class, () -> passage.clearBy(null, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> passage.clearBy(new Zaphod(), Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> passage.visibility(null, new AirState(0.1, 0.1)));
        assertThrows(IllegalArgumentException.class, () -> passage.visibility(new Flashlight(1.0), null));
    }
}
