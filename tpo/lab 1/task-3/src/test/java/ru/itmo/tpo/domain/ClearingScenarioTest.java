package ru.itmo.tpo.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ClearingScenarioTest {

    @Test
    void shouldReflectStoryScenarioMarvinClearsFaster() {
        GalleryPassage passage = new GalleryPassage(
                "collapsed-1",
                new Blockage(BlockageMaterial.ENTRAILS, 40.0),
                0.85,
                0.95
        );

        Zaphod zaphod = new Zaphod();
        Marvin marvin = new Marvin();

        passage.clearBy(zaphod, Duration.ofSeconds(2));
        double afterZaphod = passage.blockage().remainingMass();

        passage.clearBy(marvin, Duration.ofSeconds(2));
        double afterMarvin = passage.blockage().remainingMass();

        assertTrue(afterMarvin < afterZaphod);
        assertEquals(PassageState.PARTIALLY_CLEARED, passage.state());

        passage.clearBy(marvin, Duration.ofSeconds(4));
        assertEquals(PassageState.PASSABLE, passage.state());
    }
}
